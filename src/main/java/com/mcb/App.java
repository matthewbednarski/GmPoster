package com.mcb;

import com.mcb.owner.Application;
import com.ning.http.client.*;
import com.ning.http.client.Response;
import com.ning.http.client.multipart.FilePart;
import org.aeonbits.owner.ConfigFactory;
import org.jboss.netty.handler.codec.http.*;
import spark.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.Statement;
import java.util.Base64;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static spark.Spark.*;

/**
 * GmPoster App
 */
public class App {
    Logger logger = Logger.getLogger(App.class.getName());
    private final String[] args;
    private final Application cfg;

    public static void main(String[] args) {
        Logger.getGlobal().setLevel(Level.INFO);
        App app = new App(args);
        app.bootstrap();
    }

    public App(String... args) {
        this.args = args;
        this.cfg = ConfigFactory.create(Application.class);

    }

    public void bootstrap() {
        System.out.println("Server Port" + +this.cfg.port());
        port(this.cfg.port());
        if(!this.cfg.disableCors()){
           // Adds CORS headers
           enableCORS(this.cfg.corsAllowOrigin(), this.cfg.corsRequestMethod(), this.cfg.corsAllowHeaders());
        }
        post("/" + this.cfg.routeName(), (req, res) -> {
            logger.log(Level.INFO, req.body());
            ListenableFuture<Response> future = postIt(res);
            if(future != null) {
                Response r = future.get();
                r.getCookies().stream().map(cookie -> {
                    res.cookie(cookie.getName(), cookie.getValue());
                    return res;
                });
                r.getHeaders().keySet().stream()
                        .map(h -> {
                            String v = r.getHeaders(h).stream().map(hv -> hv)
                                    .collect(Collectors.joining("; "));
                            res.header(h, v);
                            return res;
                        });
                res.status(200);
                res.body(r.getResponseBody());
                res.type(r.getContentType());
                if (r.getStatusCode() == this.cfg.remoteStatusExpected()) {
                    logger.info(r.getResponseBody());
                    res.status(200);
                } else {
                    logger.warning(r.getResponseBody());
                    res.status(r.getStatusCode());
                }
                return res.body();
            }else {
                return "";
            }
        });
    }

    private void enableCORS(final String origin, final String methods, final String headers) {
        before((req, res) -> {
                    res.header("Access-Control-Allow-Origin", origin);
                    res.header("Access-Control-Request-Method", methods);
                    res.header("Access-Control-Allow-Headers", headers);
                }
        );
    }

    AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

    /***
     * Returns the name of the Authorization header
     * @return String name of the Authorization Header
     */
    private String getAuthHeaderName() {
        return cfg.remoteAuthHeaderName();
    }

    /***
     * Returns a Credential to be used in a designated Authorization header
     *
     * TODO: implement other Auth types
     *
     * @return String value of the Authorization Header
     */
    private String getAuthHeaderValue() {
        String type = cfg.remoteAuthType();
        if (type != null && "BASIC".toLowerCase().equals(type.toLowerCase())) {
            String user = cfg.remoteAuthUsername();
            String pass = cfg.remoteAuthPassword();
            String encoded = Base64.getEncoder().encodeToString((user + ':' + pass).getBytes(StandardCharsets.UTF_8));

            return "BASIC " + encoded;
        }
        return null;
    }

    /***
     * Posts to the indicated url
     *
     * TODO: implement other Auth types
     *
     * @return a @link ListenableFuture<Response>
     * @param res
     */
    private ListenableFuture<Response> postIt(spark.Response res) {


        File toPost = new File(cfg.fileToPost());
        if(!toPost.exists()){
           res.status(HttpServletResponse.SC_NO_CONTENT);
            return null;
        }
        String url = cfg.remoteUrl();
        AsyncHttpClient.BoundRequestBuilder builder = asyncHttpClient.preparePost(url)
        .addBodyPart(new FilePart(toPost.getName(), toPost));
        if(getAuthHeaderName() != null){
                    builder.addHeader(getAuthHeaderName(), getAuthHeaderValue());
        }
        ListenableFuture<Response> result = builder.execute(new AsyncCompletionHandler<Response>() {
                    @Override
                    public Response onCompleted(Response response) throws Exception {
                        return response;
                    }

                    @Override
                    public void onThrowable(Throwable t) {
                        // Something wrong happened.
                        if (logger.isLoggable(Level.FINER)) {
                            logger.log(Level.WARNING, t.getLocalizedMessage(), t);
                        } else {
                            logger.warning(t.getLocalizedMessage());
                        }
                    }
                });
        return result;
    }
}

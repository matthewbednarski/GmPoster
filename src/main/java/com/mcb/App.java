package com.mcb;

import com.mcb.owner.Application;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;
import com.ning.http.client.multipart.FilePart;
import org.aeonbits.owner.ConfigFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        get("/" + this.cfg.routeName(), (req, res) -> "Hello World");
        post("/" + this.cfg.routeName(), (req, res) -> {
            logger.log(Level.INFO, req.body());
            File toPost = new File(cfg.fileToPost());
            ListenableFuture<Response> future = postIt(cfg.remoteUrl(), toPost);
            Response r = future.get();
            logger.info(r.getResponseBody());
            r.getCookies().stream().map(cookie -> {
                res.cookie(cookie.getName(), cookie.getValue());
                return res;
            });
            res.status(200);
            res.body("Got it!!!");
            res.type("text/plain");
            return res.body();
        });
    }

    AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

    private String getAuthHeaderName() {
        return cfg.remoteAuthHeaderName();
    }

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

    private ListenableFuture<Response> postIt(String url, File file) {
        ListenableFuture<Response> result = asyncHttpClient.preparePost(url)
                .addHeader(getAuthHeaderName(), getAuthHeaderValue())
                .addBodyPart(new FilePart(file.getName(), file))
                .execute(new AsyncCompletionHandler<Response>() {
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

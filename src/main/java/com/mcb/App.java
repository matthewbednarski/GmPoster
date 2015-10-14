package com.mcb;

import com.mcb.owner.Application;
import com.ning.http.client.*;
import com.ning.http.client.multipart.FilePart;
import org.aeonbits.owner.ConfigFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static spark.Spark.*;

/**
 * GmPoster App
 */
public class App {
    static Logger logger = Logger.getLogger(App.class.getName());
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

    private static File getFile(spark.Request req, Application cfg) {
        String file = req.queryParams("file");
        if (file == null || file == "") {
            file = cfg.fileToPost();
        }
        return new File(file);
    }

    private static String getUrl(spark.Request req, Application cfg) {
        String url = req.queryParams("post_url");
        if (url == null || url == "") {
            url = cfg.remoteUrl();
        }
        return url;
    }

    private static Response processResponse(Future<Response> future, spark.Request req, spark.Response res, Application cfg) throws ExecutionException, InterruptedException {
        if (future != null) {
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
            res.type(r.getContentType());
            try {
                res.body(r.getResponseBody());
            } catch (IOException ex) {
                logger.log(Level.FINER, ex.getLocalizedMessage(), ex);
            }
            if (r.getStatusCode() == cfg.remoteStatusExpected()) {
                logger.info(res.body());
                res.status(200);
            } else {
                logger.info(res.body());
                res.status(r.getStatusCode());
            }
            return r;
        }
        return null;
    }

    public void bootstrap() {
        System.out.println("Server Port" + +this.cfg.port());
        port(this.cfg.port());
        if (!this.cfg.disableCors()) {
            // Adds CORS headers
            enableCORS(this.cfg.corsAllowOrigin(), this.cfg.corsRequestMethod(), this.cfg.corsAllowHeaders());
        }
        get("/" + this.cfg.routeName() + "/test", (req, res) -> {
            return "test request, endpoint is up";
        });
        get("/" + this.cfg.routeName() + "/stop", (req, res) -> {
            stop();
            return "stopping";
        });
        post("/" + this.cfg.routeName(), (req, res) -> {
            logger.info("Authorization: " + req.headers("Authorization"));
            req.headers().stream().map( x -> {
                logger.info(x + ": " + req.headers(x));
                return x;
            });
            logger.info(req.body());
            return "All good";
        });
        get("/" + this.cfg.routeName(), (req, res) -> {
            logger.log(Level.INFO, req.body());
            File toPost = getFile(req, cfg);
            if (!toPost.exists()) {
                logger.warning("no content to be processed; check if the file: " + cfg.fileToPost() + " exists and is readable.");
                res.status(HttpServletResponse.SC_NO_CONTENT);
            } else {
                ListenableFuture<Response> future = null;
                String customAuthHeader = req.headers(cfg.remoteAuthHeaderName());
                if (!Utils.StringUtils.isBlank(customAuthHeader)) {
                    String customAuthHeaderRename = cfg.remoteAuthHeaderRename();
                    if (!Utils.StringUtils.isBlank(customAuthHeaderRename)) {
                        future = postIt(toPost, getUrl(req, cfg), cfg.remoteAuthHeaderRename(), customAuthHeader);
                    }else{
                        future = postIt(toPost, getUrl(req, cfg), cfg.remoteAuthHeaderName(), customAuthHeader);
                    }
                }else{
                    future = postIt(toPost, getUrl(req, cfg));
                }
                Response r = processResponse(future, req, res, cfg);
                return res.body();
            }
            return "";
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
     *
     * @return String name of the Authorization Header
     */
    private String getAuthHeaderName() {
        return cfg.remoteAuthHeaderName();
    }

    /***
     * Returns a Credential to be used in a designated Authorization header
     * <p>
     * TODO: implement other Auth types
     *
     * @return String value of the Authorization Header
     */
    private String getAuthHeaderValue() {
        return cfg.remoteAuthHeaderValue();
    }

    /***
     * Returns a Credential to be used in a designated Authorization header
     * <p>
     * TODO: implement other Auth types
     *
     * @return @link Realm object for use in Authentication
     */
    private Realm getAuthRealm() {
        String type = cfg.remoteAuthType();
        if (type != null) {
            if ("BASIC".toLowerCase().equals(type.toLowerCase())) {
                String user = cfg.remoteAuthUsername();
                String pass = cfg.remoteAuthPassword();
                String encoded = Base64.getEncoder().encodeToString((user + ':' + pass).getBytes(StandardCharsets.UTF_8));

                Realm.RealmBuilder realm = new Realm.RealmBuilder();
                realm.setPassword(pass)
                        .setPrincipal(user)
                        .setScheme(Realm.AuthScheme.BASIC)
                        .setUsePreemptiveAuth(true);
                return realm.build();
            }
            if ("DIGEST".toLowerCase().equals(type.toLowerCase())) {
                String user = cfg.remoteAuthUsername();
                String pass = cfg.remoteAuthPassword();
                String encoded = Base64.getEncoder().encodeToString((user + ':' + pass).getBytes(StandardCharsets.UTF_8));

                Realm.RealmBuilder realm = new Realm.RealmBuilder();
                realm.setPassword(pass)
                        .setPrincipal(user)
                        .setScheme(Realm.AuthScheme.DIGEST)
                        .setUsePreemptiveAuth(true);
                return realm.build();
            }
        }
        return null;
    }

    private ListenableFuture<Response> postIt(File file, String url) {
        return this.postIt(file, url, null, null);
    }
    /***
     * Posts to the indicated url
     * <p>
     * TODO: implement other Auth types
     *
     * @param file
     * @return a @link ListenableFuture<Response>
     */
    private ListenableFuture<Response> postIt(File file, String url, String authHeaderName, String authHeaderValue) {
        AsyncHttpClient.BoundRequestBuilder builder = asyncHttpClient.preparePost(url)
                .addBodyPart(new FilePart(file.getName(), file));
        if(!Utils.StringUtils.isBlank(authHeaderName) && !Utils.StringUtils.isBlank(authHeaderValue)){
            builder.addHeader(authHeaderName, authHeaderValue);
        }else {
            Realm realm = getAuthRealm();
            if (realm != null) {
                builder.setRealm(realm);
            } else if (getAuthHeaderValue() != null && getAuthHeaderName() != null) {
                builder.addHeader(getAuthHeaderName(), getAuthHeaderValue());
            }
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

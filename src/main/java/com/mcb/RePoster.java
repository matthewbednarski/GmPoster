package com.mcb;

import com.mcb.base.SparkFilter;
import com.ning.http.client.*;
import com.ning.http.client.multipart.FilePart;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static spark.Spark.get;

/**
 * Created by matthewb on 10/14/15.
 */
public class RePoster extends SparkFilter {

    @Inject
    AsyncHttpClientConfig.Builder config;

    public void setup() {
        config
                .setRequestTimeout(this.getCfg().requestTimeout())
                .setConnectTimeout(this.getCfg().connectionTimeout())
                .setReadTimeout(this.getCfg().readTimeout());
        asyncHttpClient = new AsyncHttpClient(config.build());
        get("/" + this.getCfg().routeName(), (req, res) -> {
            log().log(Level.INFO, req.body());
            String file = req.queryParams("file");
            if (file == null || file == "") {
                file = this.getCfg().fileToPost();
            }
            Path toPost = Paths.get(file);
            if (!Files.exists(toPost)) {
                log().warning("no content to be processed; check if the file: " + this.getCfg().fileToPost() + " exists and is readable.");
                res.status(HttpServletResponse.SC_NO_CONTENT);
            } else {
                String url = req.queryParams("post_url");
                if (url == null || url == "") {
                    url = this.getCfg().remoteUrl();
                }
                ListenableFuture<Response> future = postIt(toPost, url);
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
                    res.body(r.getResponseBody());
                    res.type(r.getContentType());
                    if (r.getStatusCode() == this.getCfg().remoteStatusExpected()) {
                        log().info(r.getResponseBody());
                        res.status(200);
                    } else {
                        log().warning(r.getResponseBody());
                        res.status(r.getStatusCode());
                    }
                    return res.body();
                }
            }
            return "";
        });
    }

    AsyncHttpClient asyncHttpClient;

    /***
     * Returns the name of the Authorization header
     *
     * @return String name of the Authorization Header
     */
    private String getAuthHeaderName() {
        return this.getCfg().remoteAuthHeaderName();
    }

    /***
     * Returns a Credential to be used in a designated Authorization header
     * <p>
     * TODO: implement other Auth types
     *
     * @return String value of the Authorization Header
     */
    private String getAuthHeaderValue() {
        return this.getCfg().remoteAuthHeaderValue();
    }

    /***
     * Returns a Credential to be used in a designated Authorization header
     * <p>
     * TODO: implement other Auth types
     *
     * @return @link Realm object for use in Authentication
     */
    private Realm getAuthRealm() {
        String type = this.getCfg().remoteAuthType();
        if (type != null) {
            if ("BASIC".toLowerCase().equals(type.toLowerCase())) {
                String user = this.getCfg().remoteAuthUsername();
                String pass = this.getCfg().remoteAuthPassword();
                String encoded = Base64.getEncoder().encodeToString((user + ':' + pass).getBytes(StandardCharsets.UTF_8));

                Realm.RealmBuilder realm = new Realm.RealmBuilder();
                realm.setPassword(pass)
                        .setPrincipal(user)
                        .setScheme(Realm.AuthScheme.BASIC)
                        .setUsePreemptiveAuth(true);
                return realm.build();
            }
            if ("DIGEST".toLowerCase().equals(type.toLowerCase())) {
                String user = this.getCfg().remoteAuthUsername();
                String pass = this.getCfg().remoteAuthPassword();
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

    /***
     * Posts to the indicated url
     * <p>
     * TODO: implement other Auth types
     *
     * @param file
     * @return a @link ListenableFuture<Response>
     */
    private ListenableFuture<Response> postIt(Path file, String url) {
        com.ning.http.client.AsyncHttpClient.BoundRequestBuilder builder = asyncHttpClient
                .preparePost(url)
                .setRequestTimeout(this.getCfg().requestTimeout())
                .addBodyPart(new FilePart(file.getFileName().toString(), file.toFile()));
        Realm realm = getAuthRealm();
        if (realm != null) {
            builder.setRealm(realm);
        } else if (getAuthHeaderValue() != null && getAuthHeaderName() != null) {
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
                if (log().isLoggable(Level.FINER)) {
                    log().log(Level.WARNING, t.getLocalizedMessage(), t);
                } else {
                    log().warning(t.getLocalizedMessage());
                }
            }
        });
        return result;
    }
}

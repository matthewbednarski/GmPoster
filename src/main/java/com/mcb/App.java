package com.mcb;

import com.mcb.base.SparkApp;
import org.aeonbits.owner.event.ReloadEvent;
import org.aeonbits.owner.event.ReloadListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static spark.Spark.get;
import static spark.Spark.port;

/**
 * GmPoster App
 */
public class App extends SparkApp {

    private final String[] args;

    public static void main(String[] args) {
        try( InputStream is = App.class.getClassLoader().getResourceAsStream("logging.properties")) {
            if(is != null) {
                LogManager.getLogManager().readConfiguration(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger.getGlobal().setLevel(Level.INFO);
        App app = new App(args);
        app.bootstrap();
    }

    public App(String... args) {
        this.args = args;
        final App _this = this;
        this.getCfg().addReloadListener(new ReloadListener() {
            @Override
            public void reloadPerformed(ReloadEvent reloadEvent) {
                log().config("reloading " + reloadEvent.getSource().toString() );
                _this.restart();
            }
        });
    }
    private Cors cors;
    private HttpFiles files;
    private RePoster rePoster;
    private SSLConfig sslConfig;
    private BasicAuthFilter basicAuthFilter;
    @Override
    public void bootstrap() {
        sslConfig = new SSLConfig();
        cors = new Cors();
        files = new HttpFiles();
        rePoster = new RePoster();
        basicAuthFilter = new BasicAuthFilter();
        this.setup();

    }
    @Override
    public void restart(){
        this.stop();
        this.setup();
    }
    @Override
    public void stop(){
        spark.Spark.stop();
    }

    @Override
    public void setup(){

        sslConfig.setup();
        log().info("Server Port" + +this.getCfg().port());
        port(this.getCfg().port());
        get("/" + this.getCfg().routeName() + "/test", (req, res) -> {
            return "test request, endpoint is up";
        });
        get("/" + this.getCfg().routeName() + "/stop", (req, res) -> {
            spark.Spark.stop();
            return "stopping";
        });
        get("/" + this.getCfg().routeName() + "/restart", (req, res) -> {
            restart();
            return "restarting";
        });
        basicAuthFilter.setup();
        cors.setup();
        files.setup();
        rePoster.setup();
    }



}

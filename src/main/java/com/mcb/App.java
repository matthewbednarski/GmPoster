package com.mcb;

import com.mcb.base.SparkApp;

import javax.inject.Named;

import static spark.Spark.get;
import static spark.Spark.port;

/**
 * GmPoster App
 */
@Named("App")
public class App extends SparkApp {


    @Override
    public void setup() {
        log().info("Server Port" + +this.getCfg().port());
        port(this.getCfg().port());
        get("/" + this.getCfg().routeName() + "/test", (req, res) -> {
            return "test request, endpoint is up";
        });
        get("/" + this.getCfg().routeName() + "/stop", (req, res) -> {
            spark.Spark.stop();
            return "stopping";
        });
       getFilters().stream()
                .forEach(filter -> {
                    filter.setup();
                });
    }
}

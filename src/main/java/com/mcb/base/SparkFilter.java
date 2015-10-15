package com.mcb.base;

import com.google.gson.Gson;
import com.mcb.owner.SparkAppConfig;

import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * An abstract class for Spark filters to extend
 */
public abstract class SparkFilter implements ISparkFilter {

    @Inject
    SparkAppConfig config;
    @Inject
    Gson gson;
    Logger logger = Logger.getLogger(this.getClass().getName());
    protected Logger log(){
        return this.logger;
    }

    protected SparkAppConfig getCfg(){
        return config;
    }
    protected Gson getGson(){
        return this.gson;
    }
    protected String routeName(){
        return this.getCfg().routeName();
    }
}

package com.mcb.base;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mcb.owner.SparkAppConfig;
import org.aeonbits.owner.ConfigCache;

import java.util.logging.Logger;

/**
 * An abstract class for Spark filters to extend
 */
public abstract class SparkFilter implements ISparkFilter {

    private final SparkAppConfig config = ConfigCache.getOrCreate(SparkAppConfig.class,
            System.getProperties(),
            System.getenv());
    Logger logger = Logger.getLogger(this.getClass().getName());
    private Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .create();
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

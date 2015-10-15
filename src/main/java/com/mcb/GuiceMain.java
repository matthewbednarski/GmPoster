package com.mcb;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mcb.base.SparkApp;
import com.mcb.module.SparkAppModule;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by matthewb on 10/15/15.
 */
public class GuiceMain {

    public static void main(String[] args) {
        try( InputStream is = App.class.getClassLoader().getResourceAsStream("logging.properties")) {
            if(is != null) {
                LogManager.getLogManager().readConfiguration(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger.getGlobal().setLevel(Level.INFO);

//        Injector injector = Guice.createInjector(new BasicAuthModule(), new CorsModule(), new SSLConfigModule());
        Injector injector = Guice.createInjector(new SparkAppModule());
        SparkApp app = injector.getInstance(SparkApp.class);
        app.bootstrap();
    }
}

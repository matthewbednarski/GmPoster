package com.mcb.module;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.mcb.App;
import com.mcb.HttpFiles;
import com.mcb.RePoster;
import com.mcb.annotations.CorsFilter;
import com.mcb.annotations.SSLConfig;
import com.mcb.base.ISparkFilter;
import com.mcb.base.SparkApp;
import com.mcb.base.SparkAuthFilter;
import com.mcb.owner.SparkAppConfig;
import org.aeonbits.owner.ConfigCache;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by matthewb on 10/15/15.
 */
public class SparkAppModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(SparkAppConfig.class).toInstance(ConfigCache.getOrCreate(
                SparkAppConfig.class,
                System.getProperties(),
                System.getenv()
        ));
        bind(Gson.class).toInstance(
                new GsonBuilder()
                .disableHtmlEscaping()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .setPrettyPrinting()
                .create()
        );
        bind(SparkApp.class).to(App.class);
        install(new HttpClientModule());
        install(new NoAuthModule());
        install(new CorsModule());
        install(new SSLConfigModule());
    }
    @Provides
    List<ISparkFilter> sparkAppFilters(@CorsFilter ISparkFilter cors, @SSLConfig ISparkFilter sslConfig, SparkAuthFilter authFilter, HttpFiles files, RePoster rePoster){
        List<ISparkFilter> filters = new ArrayList<>();
        filters.add(sslConfig);
        filters.add(cors);
        filters.add(authFilter);
        filters.add(files);
        filters.add(rePoster);
        return filters;
    }
}

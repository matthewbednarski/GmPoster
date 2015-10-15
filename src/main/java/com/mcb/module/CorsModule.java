package com.mcb.module;

import com.google.inject.AbstractModule;
import com.mcb.annotations.CorsFilter;
import com.mcb.base.ISparkFilter;

/**
 * Created by matthewb on 10/15/15.
 */
public class CorsModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ISparkFilter.class).annotatedWith(CorsFilter.class).to(com.mcb.CorsFilter.class);
    }
}

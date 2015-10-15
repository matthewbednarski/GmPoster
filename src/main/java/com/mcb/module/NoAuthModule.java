package com.mcb.module;

import com.google.inject.AbstractModule;
import com.mcb.auth.NoAuthFilter;
import com.mcb.base.SparkAuthFilter;

/**
 * Created by matthewb on 10/15/15.
 */
public class NoAuthModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(SparkAuthFilter.class).to(NoAuthFilter.class);
    }
}

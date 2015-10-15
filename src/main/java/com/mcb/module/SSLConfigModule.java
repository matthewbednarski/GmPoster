package com.mcb.module;

import com.google.inject.AbstractModule;
import com.mcb.annotations.SSLConfig;
import com.mcb.base.ISparkFilter;

/**
 * Created by matthewb on 10/15/15.
 */
public class SSLConfigModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ISparkFilter.class).annotatedWith(SSLConfig.class).to(com.mcb.SSLConfig.class);
    }
}

package com.mcb.module;

import com.google.inject.AbstractModule;
import com.ning.http.client.AsyncHttpClientConfig;

/**
 * Created by matthew on 04.12.16.
 */
public class HttpClientModule extends AbstractModule {

    @Override
    protected void configure() {
        AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder();
        bind(AsyncHttpClientConfig.Builder.class).toInstance(builder);
    }
}

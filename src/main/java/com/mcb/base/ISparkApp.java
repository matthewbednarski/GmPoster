package com.mcb.base;

/**
 * Created by matthewb on 10/14/15.
 */
public interface ISparkApp extends ISparkFilter {
    void stop();
    void restart();
    void bootstrap();
}

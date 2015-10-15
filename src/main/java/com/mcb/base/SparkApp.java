package com.mcb.base;

import org.aeonbits.owner.event.ReloadEvent;
import org.aeonbits.owner.event.ReloadListener;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by matthewb on 10/14/15.
 */
public abstract class SparkApp extends SparkFilter implements ISparkApp {
    @Inject
    List<ISparkFilter> filters;
    protected List<ISparkFilter> getFilters(){
        return this.filters;
    }

    @Override
    public void bootstrap() {
        final SparkApp _this = this;
        this.getCfg().addReloadListener(new ReloadListener() {
            @Override
            public void reloadPerformed(ReloadEvent reloadEvent) {
                log().config("reloading " + reloadEvent.getSource().toString());
                _this.restart();
            }
        });
        this.setup();
    }

    @Override
    public void stop() {
        spark.Spark.stop();
    }

    @Override
    public void restart() {
        this.stop();
        this.setup();
    }

}

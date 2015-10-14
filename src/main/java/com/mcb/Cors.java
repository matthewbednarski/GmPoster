package com.mcb;

import com.mcb.base.SparkFilter;
import spark.utils.StringUtils;

import static spark.Spark.before;

/**
 * Created by matthewb on 10/14/15.
 */
public class Cors  extends SparkFilter {

    @Override
    public void setup() {
        if (!this.getCfg().disableCors()) {
            // Adds CORS headers
            before((req, res) -> {
                        final String origin = this.getCfg().corsAllowOrigin();
                        final String methods = this.getCfg().corsRequestMethod();
                        final String headers = this.getCfg().corsAllowHeaders();
                        if(!StringUtils.isEmpty(req.headers("Access-Control-Request-Headers"))) {
                            res.header("Access-Control-Allow-Headers", headers);
                        }
                        if(!StringUtils.isEmpty(req.headers("Access-Control-Request-Method"))) {
                            res.header("Access-Control-Allow-Methods", methods);
                        }
                        res.header("Access-Control-Allow-Credentials", Boolean.toString(true));
                        res.header("Access-Control-Allow-Origin", origin);
                    }
            );
        }
    }
}

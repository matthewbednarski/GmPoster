package com.mcb;

import com.mcb.base.SparkFilter;

import java.nio.file.Files;
import java.nio.file.Paths;

import static spark.Spark.*;

/**
 * Created by matthewb on 10/14/15.
 */
public class SSLConfig extends SparkFilter {

    @Override
    public void setup() {
        if(this.getCfg().secure()){
            String keystore = this.getCfg().keystore();
            String keystorePass = this.getCfg().keystorePassword();
            String truststore = this.getCfg().truststore();
            String truststorePass = this.getCfg().truststorePassword();
            if(Files.exists(Paths.get(keystore))
                    && Files.exists(Paths.get(truststore))
                    ) {
                log().info("Securing with SSL");
                secure(keystore, keystorePass, truststore, truststorePass);
            }else{
                log().warning("Check keystore and trustore configuration, cannot secure with SSL.");
            }

        }
    }
}

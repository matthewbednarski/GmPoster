package com.mcb;

import com.mcb.owner.Application;
import org.aeonbits.owner.ConfigFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static spark.Spark.*;
/**
 * Created by matthewb on 10/13/15.
 */
public class HttpFiles {
    Logger logger = Logger.getLogger(HttpFiles.class.getName());
    private Application cfg = ConfigFactory.create(Application.class,
            System.getProperties(),
            System.getenv());

    public void setup(){
        String httpFilesRoute = "/" + this.cfg.routeName() + "/files";
        logger.info("Adding route: " +  httpFilesRoute);
        Path filesRoot = Paths.get(this.cfg.filesRoot());
        if( !Files.exists(filesRoot) ){
            try {
                filesRoot = Files.createDirectories(filesRoot);
            } catch (IOException e) {
                if(logger.isLoggable(Level.FINER)){
                    logger.log(Level.FINER, e.getLocalizedMessage(), e);
                }else{
                    logger.log(Level.WARNING, e.getLocalizedMessage());
                }
            }
        }
        final Path finalFilesRoot = filesRoot;
        get(httpFilesRoute, (req, res) -> {
            Stream<Path> v = Files.walk(finalFilesRoot)
                    .map(a -> {
                        logger.info(a.toAbsolutePath().toString());
                        return a.toAbsolutePath();
                    });
            return v.toArray();
        });
    }
}

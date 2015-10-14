package com.mcb;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mcb.owner.Application;
import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import spark.Response;
import spark.utils.StringUtils;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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

    private Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .create();

    public void setup() {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload fileUpload = new ServletFileUpload(factory);

        String httpFilesRoute = "/" + this.cfg.routeName() + "/files";
        logger.info("Adding route: " + httpFilesRoute);
        final Path filesRoot = Paths.get(this.cfg.filesRoot());
        get(httpFilesRoute, (req, res) -> {
            if (lazyCheck(filesRoot)) {
                List<File> v = Files.walk(filesRoot)
                        .map(a -> {
                            return filesRoot.relativize(a).toString();
                        })
                        .filter(a -> spark.utils.StringUtils.isNotEmpty(a))
                        .map(a -> {
                            logger.info(a);
                            File f = new File();
                            f.setPath(a);
                            return f;
                        })
                        .collect(Collectors.toList());

                res.body(gson.toJson(v));
                return res.body();
            }
            return errorPathNotExist(filesRoot, res).body();
        });
        put(httpFilesRoute + "/*", (req, res) -> {
            if (lazyCheck(filesRoot)) {
                String part = Stream.of(req.splat())
                        .distinct()
                        .findFirst()
                        .get();
                if (!StringUtils.isEmpty(part)) {
                    Path path = filesRoot.resolve(part);
                    String ext = FilenameUtils.getExtension(path.toString());
                    if (!StringUtils.isEmpty(ext)) {
                        //Is directory path
                        path = path.getParent();
                    }
                    StringBuilder outBody = new StringBuilder();
                    List<FileItem> items = fileUpload.parseRequest(req.raw());
                    final Path dir = path;
                    items.stream()
                            .forEach(file -> {
                                logger.info(file.getName());
                                final Path savePath = dir.resolve(file.getName());
                                if (!Files.exists(savePath.getParent())) {
                                    try {
                                        Files.createDirectories(savePath.getParent());
                                        logger.info("Created directory " + savePath.getParent().toString());
                                        outBody.append("Created directory " + savePath.getParent().toString());
                                        outBody.append(System.lineSeparator());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                try {
                                    Files.write(savePath, file.get());
                                    logger.info("Saved file " + savePath.toString() + ".");
                                    outBody.append("Saved file " + savePath.toString() + ".");
                                    outBody.append(System.lineSeparator());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                    res.body(outBody.toString());
                    return res.body();
                }

            }
            return errorPathNotExist(filesRoot, res).body();
        });
        post(httpFilesRoute + "/*", (req, res) -> {
            if (lazyCheck(filesRoot)) {
                String part = Stream.of(req.splat())
                        .distinct()
                        .findFirst()
                        .get();


                if (!StringUtils.isEmpty(part)) {
                    Path path = filesRoot.resolve(part);
                    String ext = FilenameUtils.getExtension(path.toString());
                    if (!StringUtils.isEmpty(ext)) {
                        //Is directory path
                        path = path.getParent();
                    }
                    StringBuilder outBody = new StringBuilder();
                    List<FileItem> items = fileUpload.parseRequest(req.raw());
                    final Path dir = path;
                    items.stream()
                            .forEach(file -> {
                                logger.info(file.getName());
                                final Path savePath = dir.resolve(file.getName());
                                if (!Files.exists(savePath.getParent())) {
                                    try {
                                        Files.createDirectories(savePath.getParent());
                                        logger.info("Created directory " + savePath.getParent().toString());
                                        outBody.append("Created directory " + savePath.getParent().toString());
                                        outBody.append(System.lineSeparator());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                try {
                                    Files.write(savePath, file.get());
                                    logger.info("Saved file " + savePath.toString() + ".");
                                    outBody.append("Saved file " + savePath.toString() + ".");
                                    outBody.append(System.lineSeparator());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                    res.body(outBody.toString());
                    return res.body();
                }

            }
            return errorPathNotExist(filesRoot, res).body();
        });
        get(httpFilesRoute + "/*", (req, res) -> {
            if (lazyCheck(filesRoot)) {
                String part = Stream.of(req.splat())
                        .distinct()
                        .findFirst()
                        .get();
                byte[] bytes = null;
                if (!StringUtils.isEmpty(part)) {
                    Path path = Paths.get(part);
                    if (Files.exists(filesRoot.resolve(path))) {
                        bytes = Files.readAllBytes(filesRoot.resolve(path));
                        String ct = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(filesRoot.resolve(path).toFile());
                        FileTypeMap map = MimetypesFileTypeMap.getDefaultFileTypeMap();
                        logger.info("Content-Type: " + ct);

                        res.type(ct);
                    }
                }
                if (bytes != null) {
                    try (OutputStream os = res.raw().getOutputStream()) {
                        os.write(bytes);
                    } catch (IOException e) {
                        halt(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                    return res.raw();
                }
            }
            return errorPathNotExist(filesRoot, res).body();
        });
    }

    private Response errorPathNotExist(Path path, Response res) {
        res.status(HttpServletResponse.SC_EXPECTATION_FAILED);
        res.body(path.toString() + " does not exist.");
        return res;
    }

    private boolean lazyCheck(Path dir) {
        boolean result = false;
        if (Files.isDirectory(dir)) {
            result = true;
        } else if (!Files.exists(dir)) {
            try {
                Path createdDir = Files.createDirectories(dir);
                if (Files.isDirectory(createdDir)) {
                    result = true;
                }
            } catch (IOException e) {
                if (logger.isLoggable(Level.FINER)) {
                    logger.log(Level.FINER, e.getLocalizedMessage(), e);
                } else {
                    logger.log(Level.WARNING, e.getLocalizedMessage());
                }
            }
        }
        return result;
    }

    private static class File {
        private String path;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }
}

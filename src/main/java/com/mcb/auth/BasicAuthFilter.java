package com.mcb.auth;

import com.mcb.base.SparkAuthFilter;
import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.util.security.Constraint;
import spark.utils.StringUtils;

import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static spark.Spark.*;

/**
 * Created by matthewb on 10/14/15.
 */
public class BasicAuthFilter extends SparkAuthFilter {

    @Override
    public void setup() {
        ConstraintSecurityHandler security = new ConstraintSecurityHandler();
        Constraint constraint = new Constraint();
        constraint.setName("auth");
        constraint.setAuthenticate(true);
        constraint.setRoles(this.getCfg().serverAuthRoles());
        List<ConstraintMapping> mappings = Stream.of(this.getCfg().serverAuthPaths())
                .map(path -> {
                    ConstraintMapping mapping = new ConstraintMapping();
                    mapping.setConstraint(constraint);
                    if (path.contains("--")) {
                        String[] parts = StringUtils.delimitedListToStringArray(path, "--");
                        mapping.setPathSpec(parts[0]);
                        mapping.setMethod(parts[1]);
                    } else {
                        mapping.setPathSpec(path);
                    }
                    return mapping;
                })
                .collect(Collectors.toList());

        security.setConstraintMappings(mappings);
        security.setAuthenticator(new BasicAuthenticator());
        String serverAuthRealmProperties = resourceFromString(this.getCfg().serverAuthRealmProperties());
        final HashLoginService loginService = new HashLoginService(this.getCfg().serverAuthRealm(), serverAuthRealmProperties);
        loginService.setHotReload(true);
        security.setLoginService(loginService);
        try {
            security.start();
            before(this.routeName() + "/*", (req, res) -> {
                Authenticator authenticator = security.getAuthenticator();
                Authentication authentication = authenticator.validateRequest(req.raw(), res.raw(), true);
                if (authentication instanceof Authentication.User) {
                    if (security.getAuthenticator().secureResponse(req.raw(), res.raw(), true, (Authentication.User) authentication)) {
                        log().finest("Logged in user: " + ((Authentication.User)authentication).getUserIdentity().getUserPrincipal().getName() );
                    } else {
                        log().info("Could not authorize request");
                    }
                }
            });
            before("logout", (req, res) -> {
                res.removeCookie("JSESSIONID");
//                res.header(HttpHeader.WWW_AUTHENTICATE.asString(), "basic realm=\"" + loginService.getName() + '"');
                halt(401);
            });
        } catch (Exception e) {
            if(log().isLoggable(Level.FINER)){
                log().log(Level.FINER, e.getLocalizedMessage(), e);
            }else{
                log().severe(e.getLocalizedMessage());
            }
        }
    }

    private static String resourceFromString(String res) {
        URL properties = BasicAuthFilter.class.getClassLoader().getResource(res);
        if (properties != null) {
            return Paths.get(URI.create(properties.toString())).toString();
        } else if (Files.exists(Paths.get(res))) {
            return Paths.get(URI.create(res)).toAbsolutePath().toString();
        }
        return URI.create(res).toString();
    }
}

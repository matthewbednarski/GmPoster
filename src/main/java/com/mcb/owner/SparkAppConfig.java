
package com.mcb.owner;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.HotReload;
import org.aeonbits.owner.Config.HotReloadType;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Reloadable;

import java.util.concurrent.TimeUnit;


@Config.LoadPolicy(Config.LoadType.FIRST)
@Sources(
        {
        "file:${server.app.name}.properties",
        "file:~/${server.app.name}.properties",
        "file:/etc/${server.app.name}.properties",
        "classpath:${server.app.name}.properties",
        "classpath:application.properties",
        "classpath:com/mcb/owner/SparkAppConfig.properties" })
@HotReload(value = 500L, unit = TimeUnit.MILLISECONDS, type = HotReloadType.ASYNC)
public interface SparkAppConfig extends org.aeonbits.owner.Config,Reloadable {

	@Key( "server.app.name")
    @DefaultValue("gm")
    String appName();

    @Key( "server.secure" )
    @DefaultValue("false")
    boolean secure();
    @Key( "server.secure.keystore" )
    String keystore();
    @Key( "server.secure.keystore.password" )
    String keystorePassword();
    @Key( "server.secure.truststore" )
    String truststore();
    @Key( "server.secure.truststore.password" )
    String truststorePassword();
    @Key( "server.auth.realm" )
    @DefaultValue("spark-app")
    String serverAuthRealm();
    @Key( "server.auth.realm.properties" )
    @DefaultValue("realm.properties")
    String serverAuthRealmProperties();

    @Key( "server.auth.roles" )
    @DefaultValue("user, admin")
    String[] serverAuthRoles();
    @Key( "server.auth.paths" )
    @DefaultValue("/*--POST, /*--PUT, /*--DELETE")
    String[] serverAuthPaths();

	@Key( "server.port" )
    @DefaultValue("8080")
	int port();

    @Key( "server.cors.disable" )
    @DefaultValue( "False" )
    Boolean disableCors();

    @Key( "server.cors.allow.origin" )
    @DefaultValue( "*" )
    String corsAllowOrigin();

    @Key( "server.cors.request.method" )
    @DefaultValue( "*" )
    String corsRequestMethod();

    @Key( "server.cors.allow.headers" )
    @DefaultValue( "*" )
    String corsAllowHeaders();

    @Key( "app.route.name" )
    @DefaultValue( "route" )
    String routeName();

    @Key( "app.file.to.post" )
	String fileToPost();

    @Key( "app.remote.url" )
	String remoteUrl();
    @Key( "app.remote.auth.type" )
	String remoteAuthType();
    @Key( "app.remote.auth.username" )
	String remoteAuthUsername();
    @Key( "app.remote.auth.password" )
	String remoteAuthPassword();

    @Key( "app.remote.auth.header" )
    String remoteAuthHeaderName();
    @Key( "app.remote.auth.header.value" )
    String remoteAuthHeaderValue();

    @Key( "app.remote.status.expected" )
    @DefaultValue("201")
    int remoteStatusExpected();


    @Key( "files.root" )
    @DefaultValue("${HOME}/${app.route.name}/files")
    String filesRoot();

    @Config.Key( "server.request.timeout" )
    @Config.DefaultValue( "90000" )
    int requestTimeout();

    @Config.Key( "server.read.timeout" )
    @Config.DefaultValue( "60000" )
    int readTimeout();

    @Config.Key( "server.connection.timeout" )
    @Config.DefaultValue( "10000" )
    int connectionTimeout();
}

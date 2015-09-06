
package com.mcb.owner;

import org.aeonbits.owner.Config;

@Config.Sources({
        "file:gm.properties",
        "file:~/.gm.properties",
        "file:/etc/gm.properties",
        "classpath:com/mcb/owner/Application.properties" })
public interface Application extends Config {

	@Key( "server.port" )
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

    @Key( "app.remote.status.expected" )
    @DefaultValue("201")
    int remoteStatusExpected();


}

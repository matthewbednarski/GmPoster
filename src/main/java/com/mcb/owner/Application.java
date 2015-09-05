
package com.mcb.owner;

import org.aeonbits.owner.Config;

public interface Application extends Config {

	@Key( "server.port" )
	@DefaultValue( "6547" )
	int port();

    @Key( "server.auth.header" )
    String authHeader();

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
}

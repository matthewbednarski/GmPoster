GmPoster
========

A [Spark Framework](http://sparkjava.com/) app for posting local files to a remote server for locally installed HTML5 apps.

Build
-----

```bash
mvn clean package
```

Run with Java
-------------

```bash
java -jar GmPoster-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Configuration
-------------

This is the [Owner Config](http://owner.aeonbits.org/docs/loading-strategies/) interface from which the settings are taken.

```java
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

    @Key( "app.remote.auth.header.value" )
    String remoteAuthHeaderValue();

    @Key( "app.remote.status.expected" )
    @DefaultValue("201")
    int remoteStatusExpected();
}

```

```properties
#################################################################
#
# Settaggi relativi al server locale
#
#################################################################
server.port=8889
## per default CORS e' abilitato (false)
#server.cors.disable=false
#res.header("Access-Control-Allow-Origin", origin);
server.cors.allow.origin=*
#res.header("Access-Control-Request-Method", methods);
server.cors.request.method=*
#res.header("Access-Control-Allow-Headers", headers);
server.cors.allow.headers=*

#################################################################
#
# Settaggi relativi al API esposto dal server
#
#################################################################
# la parte del url dopo la porta ie http://localhost:8080/[route-name]/
app.route.name=gm
# questo dovrebbe essere il valore del file to post, se e' statico
app.file.to.post=pom.xml


#################################################################
#
# Settaggi relativi alla richiesta per caricare l'immagine
#
#################################################################

# url verso il quale si deve fare il POST
app.remote.url=http://localhost:8080/api/item/img
# stato http restituito dalla richiesta che indica che la chiamata e' andata a buon fine
app.remote.status.expected=201
# Ho implementato soltanto Basic/Digest authentication ma puo' essere facilmente esteso; altrimenti si usa direttamente app.remote.auth.header
app.remote.auth.type=Basic
app.remote.auth.username=matt
app.remote.auth.password=pwd1123

# per flessibilita' si puo' impostare direttamente l'header di usare per authenticazione
#app.remote.auth.header=Authorization
# il valore del app.remote.auth.header
#app.remote.auth.header.value=token dsafsafeakfjlasvjsavaslk
```

As you can see looking at the `@Config.Sources` annotation, the order in which the application looks for files to load is the following:

1. `file:gm.properties`, ie. a file named gm.properties in the `%CD%` (`pwd`),
2. `file:~/.gm.properties`, ie. a file named gm.properties in the user's home direcotry,
3. `file:/etc/gm.properties`, ie. a file named gm.properties in the folder `/etc/`,
4. `classpath:com/mcb/owner/Application.properties`, lastly, a file `Application.properties` on the classpath in the `package` `com.mcb.owner`.

NB. the `Config` will stop looking for `properties` files as soon as it finds one.






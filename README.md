GmPoster
========

A [Spark Framework](http://sparkjava.com/) app for posting local files to a remote server for locally installed HTML5 apps.

Configuration
-------------

```
# GmPoster server settings
server.port=8889

# app settings
# http://localhost:[port]/[route]
app.route.name=gm

app.file.to.post=pom.xml

app.remote.url=http://localhost:8080/api/item/img
app.remote.status.expected=201
app.remote.auth.type=Basic
app.remote.auth.header=Authorization
app.remote.auth.username=matt
app.remote.auth.password=pwd1123
```


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





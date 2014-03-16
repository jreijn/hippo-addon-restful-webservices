# Hippo Web services project

[![Build Status](https://travis-ci.org/jreijn/hippo-addon-webservices.png?branch=master)](https://travis-ci.org/jreijn/hippo-addon-webservices)

This project is a pragmatic approach to provide web services on top of the Hippo Repository.

It requires basic authentication and you can use the default user name and password to login e.g (admin/admin).

This project uses [Swagger](https://helloreverb.com/developers/swagger) for the documentation of the APIs.
Visit [http://localhost:8080/cms/swagger/](http://localhost:8080/cms/swagger/) after startup to see the available service end-points and their documentation.


Running locally
===============

This project uses the Maven Cargo plugin to run the CMS and site locally in Tomcat.
From the project root folder, execute:

```
  $ mvn clean install
  $ mvn -P cargo.run
```

Access the CMS at http://localhost:8080/cms, and the site at http://localhost:8080/site
Logs are located in target/tomcat6x/logs

Also you can see the WEB api docs by browsing to http://localhost:8080/cms/swagger/


Using JRebel
============

Set the environment variable REBEL_HOME to the directory containing jrebel.jar.

Build with:

```
$ mvn clean install -Djrebel
```

Start with:

```
$ mvn -P cargo.run -Djrebel
```

Best Practice for development
=============================

Use the option -Drepo.path=/some/path/to/repository during start up. This will avoid
your repository to be cleared when you do a mvn clean.

For example start your project with:
```
$ mvn -P cargo.run -Drepo.path=/home/usr/tmp/repo
```
or with jrebel:

```
$ mvn -P cargo.run -Drepo.path=/home/usr/tmp/repo -Djrebel
```

Hot deploy
==========

To hot deploy, redeploy or undeploy the CMS or site:
```
  $ cd cms (or site)
  $ mvn cargo:redeploy (or cargo:undeploy, or cargo:deploy)
```
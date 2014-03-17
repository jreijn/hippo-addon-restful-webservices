# Hippo RESTful Web services project

[![Build Status](https://travis-ci.org/jreijn/hippo-addon-webservices.png?branch=master)](https://travis-ci.org/jreijn/hippo-addon-webservices)

This project is a pragmatic approach to provide RESTful web services on top of the Hippo CMS repository.

It provides amongst others a full CRUD REST API for JCR nodes and properties.

It requires basic authentication and you can use the default user name and password to login e.g (admin/admin).

This project uses [Swagger](https://helloreverb.com/developers/swagger) for the documentation of the APIs.
Visit [http://localhost:8080/cms/swagger/](http://localhost:8080/cms/swagger/) after startup to see the available service end-points and their documentation.

## Currently Available APIs

+ Nodes API ```/nodes```
+ Properties API ```/properties```
+ Query API ```/queries```
+ System API ```/system```

## Possible future plans

+ Support [Richardson Maturity Model](http://martinfowler.com/articles/richardsonMaturityModel.html) and HATEOAS
+ Add CORS support


## Running locally

This project uses the Maven Cargo plugin to run the CMS and site locally in Tomcat.
From the project root folder, execute:

```
  $ mvn clean package
  $ mvn -P cargo.run
```

Access the CMS at http://localhost:8080/cms, and the site at http://localhost:8080/site
Logs are located in target/tomcat6x/logs

Also you can see the WEB api docs by browsing to http://localhost:8080/cms/swagger/

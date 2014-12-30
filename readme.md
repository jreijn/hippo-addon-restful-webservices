# Hippo RESTful web services add-on

[![Build Status](https://travis-ci.org/jreijn/hippo-addon-restful-webservices.png?branch=master)](https://travis-ci.org/jreijn/hippo-addon-restful-webservices)
[![Coverage Status](https://coveralls.io/repos/jreijn/hippo-addon-restful-webservices/badge.png?branch=master)](https://coveralls.io/r/jreijn/hippo-addon-restful-webservices?branch=master)

This project is a pragmatic approach to provide web services on top of the Hippo CMS repository.

It provides amongst others a full CRUD API for JCR nodes and properties.

## Current Available APIs

+ Info ```/```
+ Nodes API ```/nodes```
+ Properties API ```/properties```
+ Query API ```/_query```
+ System API ```/_system```
+ Statistics API ```/_stats```

The resources have been defined into two types:

+ CRUD resources (without underscore)
+ Non-CRUD resources (with underscore)

## Future plans

+ Support [Richardson Maturity Model](http://martinfowler.com/articles/richardsonMaturityModel.html) and HATEOAS to be fully RESTful.
+ Add CORS support so the API can be used from a Javascript app __(Done)__

## Using the web services in your project

See for a working demo the [sample project](https://github.com/jreijn/hippo-addon-restful-webservices-demo).


To install this project into a local project you need to add the web services dependency to your projects _cms_ module located in ```cms/pom.xml```

``` xml
<dependency>
  <groupId>org.onehippo.forge.webservices</groupId>
  <artifactId>hippo-addon-restful-webservices</artifactId>
  <version>0.2.0</version>
</dependency>
```

Add the following repository to your repository section in your pom.xml

``` xml
<repositories>
  <repository>
    <id>sonatype-oss-public</id>
    <url>https://oss.sonatype.org/content/groups/public/</url>
    <releases>
      <enabled>true</enabled>
    </releases>
    <snapshots>
      <enabled>false</enabled>
    </snapshots>
  </repository>
<repositories>
```

Now add the servlet definition to your CMS web.xml located in ```cms/src/main/webapp/WEB-INF/web.xml```.

``` xml
<servlet>
  <servlet-name>RepositoryWebServicesServlet</servlet-name>
  <servlet-class>org.apache.cxf.jaxrs.servlet.CXFNonSpringJaxrsServlet</servlet-class>
  <init-param>
    <param-name>jaxrs.serviceClasses</param-name>
    <param-value>
      org.onehippo.forge.webservices.jaxrs.RootResource,
      org.onehippo.forge.webservices.jaxrs.system.SystemResource,
      org.onehippo.forge.webservices.jaxrs.jcr.NodesResource,
      org.onehippo.forge.webservices.jaxrs.jcr.PropertiesResource,
      org.onehippo.forge.webservices.jaxrs.jcr.QueryResource,
      org.onehippo.forge.webservices.jaxrs.StatsResource
    </param-value>
  </init-param>
  <init-param>
    <param-name>jaxrs.providers</param-name>
    <param-value>
      org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider,
      org.apache.cxf.rs.security.cors.CrossOriginResourceSharingFilter,
      org.onehippo.forge.webservices.jaxrs.exception.CustomWebApplicationExceptionMapper,
      org.onehippo.forge.webservices.HippoAuthenticationRequestHandler
    </param-value>
  </init-param>
  <init-param>
    <param-name>jaxrs.extensions</param-name>
    <param-value>json=application/json, xml=application/xml</param-value>
  </init-param>
  <load-on-startup>6</load-on-startup>
</servlet>
```

We will also need to add the servlet mapping, so that the API is exposed at _/rest/api_ :


``` xml
<servlet-mapping>
  <servlet-name>RepositoryWebServicesServlet</servlet-name>
  <url-pattern>/rest/api/*</url-pattern>
</servlet-mapping>
```

That's it. Now the web services should be available. In case you are using the default archetype you should be able to get
a response by calling the root endpoint [http://localhost:8080/cms/rest/api/](http://localhost:8080/cms/rest/api/)

For a working example see this the [demo project repository](https://github.com/jreijn/hippo-addon-restful-webservices-demo) on GitHub.

## <a name="source"></a>Building from source

This plugin requires [Maven](http://maven.apache.org) to build the module from source.

After you have installed Maven you can build the module with:

``` console
$ mvn install
```

## Issues

If you have any problems, please [check the project issues](https://github.com/jreijn/hippo-addon-restful-webservices/issues).

## Contributions

Pull requests are, of course, very welcome! Head over to the [open issues](https://github.com/jreijn/hippo-addon-restful-webservices/issues) to see what we need help with. Make sure you let us know if you intend to work on something. Also, check out the [milestones](https://github.com/jreijn/hippo-addon-restful-webservices/issues/milestones) to see what is planned for future releases.

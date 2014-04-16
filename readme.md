# Hippo RESTful Web services project

[![Build Status](https://travis-ci.org/jreijn/hippo-addon-webservices.png?branch=master)](https://travis-ci.org/jreijn/hippo-addon-webservices)
[![Coverage Status](https://coveralls.io/repos/jreijn/hippo-addon-webservices/badge.png?branch=master)](https://coveralls.io/r/jreijn/hippo-addon-webservices?branch=master)

This project is a pragmatic approach to provide RESTful web services on top of the Hippo CMS repository.

It provides amongst others a full CRUD REST API for JCR nodes and properties.

## Current Available APIs

+ Nodes API ```/nodes```
+ Properties API ```/properties```
+ Query API ```/query```
+ System API ```/system```
+ HelloWorld API ```/hello```

## Future plans

+ Support [Richardson Maturity Model](http://martinfowler.com/articles/richardsonMaturityModel.html) and HATEOAS?
+ Add CORS support so the API can be used from a Javascript app __(Done)__

## Using the web services in your project

See for a working demo the [sample project](https://github.com/jreijn/hippo-addon-webservices-demo).

Add the web services dependency to your projects cms module located in ```cms/pom.xml```

```
<dependency>
  <groupId>org.onehippo.forge.webservices</groupId>
  <artifactId>hippo-addon-webservices</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Now add the servlet definition to your CMS web.xml located in ```cms/src/main/webapp/WEB-INF/web.xml```.

```
<servlet>
  <servlet-name>RepositoryWebServicesServlet</servlet-name>
  <servlet-class>org.apache.cxf.jaxrs.servlet.CXFNonSpringJaxrsServlet</servlet-class>
  <init-param>
    <param-name>jaxrs.serviceClasses</param-name>
    <param-value>
      org.onehippo.forge.webservices.jaxrs.HelloResource,
      org.onehippo.forge.webservices.jaxrs.system.SystemResource,
      org.onehippo.forge.webservices.jaxrs.jcr.NodesResource,
      org.onehippo.forge.webservices.jaxrs.jcr.PropertiesResource,
      org.onehippo.forge.webservices.jaxrs.jcr.QueryResource
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

Next add the servlet mapping to the cms __web.xml__ :


```
<servlet-mapping>
  <servlet-name>RepositoryWebServicesServlet</servlet-name>
  <url-pattern>/rest/api/*</url-pattern>
</servlet-mapping>

```

That's it. Now the web services should be available. In case you are using the default archetype you should be able to get
a response by calling [http://localhost:8080/cms/rest/api/hello](http://localhost:8080/cms/rest/api/hello)

For a working example see this the [demo project repository](https://github.com/jreijn/hippo-addon-webservices-demo) on GitHub.

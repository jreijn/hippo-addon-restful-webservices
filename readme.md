# Hippo RESTful Web services add-on

[![Build Status](https://travis-ci.org/jreijn/hippo-addon-webservices.png?branch=master)](https://travis-ci.org/jreijn/hippo-addon-webservices)

This project is a pragmatic approach to provide RESTful web services on top of the Hippo CMS repository.

It provides amongst others a full CRUD REST API for JCR nodes and properties.

It requires basic authentication and you can use the default user name and password to login e.g (admin/admin).

This project uses [Swagger](https://helloreverb.com/developers/swagger) for the documentation of the APIs.
Visit [http://localhost:8080/cms/swagger/](http://localhost:8080/cms/swagger/) after startup to see the available service end-points and their documentation.

## Currently Available APIs

+ Nodes API ```/nodes```
+ Properties API ```/properties```
+ Query API ```/query```
+ System API ```/system```
+ HelloWorld API ```/hello```

## Possible future plans

+ Support [Richardson Maturity Model](http://martinfowler.com/articles/richardsonMaturityModel.html) and HATEOAS?
+ Add CORS support?
+


## Using the webservices in your project

See for a working demo the sample project.

Add the webservices dependency to your projects cms module located in ```cms/pom.xml```

```
<dependency>
  <groupId>org.onehippo.forge.webservices</groupId>
  <artifactId>addon-webservices</artifactId>
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

We will also need to add the servlet mapping, so that the API is exposed at _/rest/api_ :


```
<servlet-mapping>
  <servlet-name>RepositoryWebServicesServlet</servlet-name>
  <url-pattern>/rest/api/*</url-pattern>
</servlet-mapping>
```

That's it. Now the web services should be available. In case you are using the default archetype you should be able to get
a response by calling [http://localhost:8080/cms/rest/api/hello](http://localhost:8080/cms/rest/api/hello)

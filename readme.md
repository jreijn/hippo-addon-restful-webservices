# Hippo RESTful web services add-on

[![Build Status](https://travis-ci.org/jreijn/hippo-addon-restful-webservices.png?branch=master)](https://travis-ci.org/jreijn/hippo-addon-restful-webservices)
[![Coverage Status](https://coveralls.io/repos/jreijn/hippo-addon-restful-webservices/badge.png?branch=master)](https://coveralls.io/r/jreijn/hippo-addon-restful-webservices?branch=master)

This project is a pragmatic approach to provide web services on top of the Hippo CMS repository.

It provides amongst others a full CRUD API for JCR nodes and properties.

## Current Available APIs

Version 0.3.X

+ Info ```/```
+ Nodes API ```/nodes```
+ Properties API ```/properties```
+ Query API ```/_query```
+ System API ```/_system```
+ Statistics API ```/_stats```
+ Users API ```/users```
+ Groups API ```/groups```

The resources have been defined into two types:

+ CRUD resources (without underscore)
+ Non-CRUD resources (with underscore)

If you would like to know more about how to use the REST endpoints go ahead and read the [API Reference section](https://github.com/jreijn/hippo-addon-restful-webservices/wiki/API-Reference).

## Using the web services in your project

See for a working demo the [sample project](https://github.com/jreijn/hippo-addon-restful-webservices-demo).

To install this project into a local project you need to add the web services dependency to your projects _cms_ module located in ```cms/pom.xml```

``` xml
<dependency>
  <groupId>org.onehippo.forge.webservices</groupId>
  <artifactId>hippo-addon-restful-webservices</artifactId>
  <version>0.3.1</version>
</dependency>
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
      org.onehippo.forge.webservices.jaxrs.StatsResource,
      org.onehippo.forge.webservices.jaxrs.management.UsersResource,
      org.onehippo.forge.webservices.jaxrs.management.GroupsResource
    </param-value>
  </init-param>
  <init-param>
    <param-name>jaxrs.providers</param-name>
    <param-value>
      org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider,
      org.apache.cxf.rs.security.cors.CrossOriginResourceSharingFilter,
      org.onehippo.forge.webservices.jaxrs.exception.CustomWebApplicationExceptionMapper,
      org.onehippo.forge.webservices.jaxrs.CustomJacksonConfig,
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

## Issues, Questions or improvements

If you find any problems, have a question or see a possibility to improve the add-on please browse the [project issues](https://github.com/jreijn/hippo-addon-restful-webservices/issues).

## Contributions

Pull requests are, of course, very welcome! Head over to the [open issues](https://github.com/jreijn/hippo-addon-restful-webservices/issues) to see what we need help with. Make sure you let us know if you intend to work on something. Also, check out the [milestones](https://github.com/jreijn/hippo-addon-restful-webservices/issues/milestones) to see what is planned for future releases.

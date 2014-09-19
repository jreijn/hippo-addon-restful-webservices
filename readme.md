# Hippo web services add-on

[![Build Status](https://travis-ci.org/jreijn/hippo-addon-webservices.png?branch=master)](https://travis-ci.org/jreijn/hippo-addon-webservices)
[![Coverage Status](https://coveralls.io/repos/jreijn/hippo-addon-webservices/badge.png?branch=master)](https://coveralls.io/r/jreijn/hippo-addon-webservices?branch=master)

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

See for a working demo the [sample project](https://github.com/jreijn/hippo-addon-webservices-demo).

Add the web services dependency to your projects cms module located in ```cms/pom.xml```

```
<dependency>
  <groupId>org.onehippo.forge.webservices</groupId>
  <artifactId>hippo-addon-webservices</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Because this plugin is not yet available in a public Maven repository you will have to [build it from source](#source) before you can use it.

Now add the servlet definition to your CMS web.xml located in ```cms/src/main/webapp/WEB-INF/web.xml```.

```
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

For a working example see this the [demo project repository](https://github.com/jreijn/hippo-addon-webservices-demo) on GitHub.

## <a name="source"></a>Building from source

Since this plugin is not yet released you will have to build it from source before you can use it.

This plugin requires [Maven](http://maven.apache.org) to build the module from source.

After you have installed Maven you can build the module with:

```
$ mvn install
```


## Issues

If you have any problems, please [check the project issues](https://github.com/jreijn/hippo-addon-webservices/issues).

## Contributions

Pull requests are, of course, very welcome! Head over to the [open issues](https://github.com/jreijn/hippo-addon-webservices/issues) to see what we need help with. Make sure you let us know if you intend to work on something. Also, check out the [milestones](https://github.com/jreijn/hippo-addon-webservices/issues/milestones) to see what is planned for future releases.

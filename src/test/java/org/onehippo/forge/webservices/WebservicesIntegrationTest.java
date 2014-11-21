/*
 * Copyright 2014 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onehippo.forge.webservices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wordnik.swagger.jaxrs.listing.ApiDeclarationProvider;
import com.wordnik.swagger.jaxrs.listing.ApiListingResourceJSON;
import com.wordnik.swagger.jaxrs.listing.ResourceListingProvider;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharingFilter;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.hippoecm.repository.HippoRepositoryFactory;
import org.junit.After;
import org.junit.Before;
import org.onehippo.forge.webservices.jaxrs.RootResource;
import org.onehippo.forge.webservices.jaxrs.StatsResource;
import org.onehippo.forge.webservices.jaxrs.exception.CustomWebApplicationExceptionMapper;
import org.onehippo.forge.webservices.jaxrs.jcr.NodesResource;
import org.onehippo.forge.webservices.jaxrs.jcr.PropertiesResource;
import org.onehippo.forge.webservices.jaxrs.jcr.QueryResource;
import org.onehippo.forge.webservices.jaxrs.system.SystemResource;
import org.onehippo.repository.testutils.RepositoryTestCase;

/**
 * Test case which runs an embedded Jetty and an Hippo Repository for integration level testing the webservices.
 */
public abstract class WebservicesIntegrationTest extends RepositoryTestCase {

    protected static final String HTTP_ENDPOINT_ADDRESS = "http://localhost:8080/cms/rest/api";
    protected static Server cxfServer;
    protected static WebClient client;

    @Before
    public void setUp() throws Exception {
        HippoRepositoryFactory.setDefaultRepository(background);
        super.setUp();
        startServer();
        client = WebClient.create(HTTP_ENDPOINT_ADDRESS, Collections.singletonList(new JacksonJaxbJsonProvider()), "admin", "admin", null);
    }

    private static void startServer() throws Exception {
        Object jacksonJaxbJsonProvider = new JacksonJaxbJsonProvider();
        Object crossOriginResourceSharingFilter = new CrossOriginResourceSharingFilter();
        Object customWebApplicationExceptionMapper = new CustomWebApplicationExceptionMapper();
        Object hippoAuthenticationRequestHandler = new HippoAuthenticationRequestHandler();
        Object apiDeclarationProvider = new ApiDeclarationProvider();
        Object resourceListingProvider = new ResourceListingProvider();

        List<Object> providers = new ArrayList<Object>();
        providers.add(jacksonJaxbJsonProvider);
        providers.add(crossOriginResourceSharingFilter);
        providers.add(customWebApplicationExceptionMapper);
        providers.add(hippoAuthenticationRequestHandler);
        providers.add(apiDeclarationProvider);
        providers.add(resourceListingProvider);

        List<Class<?>> serviceClasses = new ArrayList<Class<?>>();
        serviceClasses.add(ApiListingResourceJSON.class);
        serviceClasses.add(RootResource.class);
        serviceClasses.add(SystemResource.class);
        serviceClasses.add(NodesResource.class);
        serviceClasses.add(PropertiesResource.class);
        serviceClasses.add(QueryResource.class);
        serviceClasses.add(StatsResource.class);

        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();

        sf.setProviders(providers);
        sf.setResourceClasses(serviceClasses);
        sf.setAddress(HTTP_ENDPOINT_ADDRESS);
        cxfServer = sf.create();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        if (cxfServer != null) {
            cxfServer.stop();
            cxfServer.destroy();
        }
    }

}
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.wordnik.swagger.jaxrs.listing.ApiDeclarationProvider;
import com.wordnik.swagger.jaxrs.listing.ApiListingResourceJSON;
import com.wordnik.swagger.jaxrs.listing.ResourceListingProvider;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.rs.security.cors.CorsHeaderConstants;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharingFilter;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.hippoecm.repository.HippoRepository;
import org.hippoecm.repository.HippoRepositoryFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onehippo.forge.webservices.jaxrs.exception.CustomWebApplicationExceptionMapper;
import org.onehippo.forge.webservices.jaxrs.jcr.NodesResource;
import org.onehippo.forge.webservices.jaxrs.jcr.PropertiesResource;
import org.onehippo.forge.webservices.jaxrs.jcr.QueryResource;
import org.onehippo.forge.webservices.jaxrs.jcr.model.JcrNode;
import org.onehippo.forge.webservices.jaxrs.jcr.model.JcrProperty;
import org.onehippo.forge.webservices.jaxrs.jcr.model.JcrQueryResult;
import org.onehippo.forge.webservices.jaxrs.jcr.model.JcrSearchQuery;
import org.onehippo.forge.webservices.jaxrs.system.SystemResource;
import org.onehippo.repository.testutils.RepositoryTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test case which runs an embedded Jetty and a Hippo Repository for integration level testing the webservices.
 */
public class WebservicesIntegrationTest extends RepositoryTestCase {

    private final static String HTTP_ENDPOINT_ADDRESS = "http://localhost:8080/rest";
    private final static String DEFAULT_REPO_LOCATION_PROTOCOL = "file://";
    private static Server server;
    private static Logger log = LoggerFactory.getLogger(WebservicesIntegrationTest.class);
    private static WebClient client;
    static HippoRepository hippoRepository;

    @BeforeClass
    public static void initialize() throws Exception {
        setUpClass();
        startServer();
        hippoRepository = HippoRepositoryFactory.getHippoRepository();
        HippoRepositoryFactory.setDefaultRepository(hippoRepository);
    }

    @Before
    public void setUp() throws Exception {
        setUp(false);
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
        serviceClasses.add(SystemResource.class);
        serviceClasses.add(NodesResource.class);
        serviceClasses.add(PropertiesResource.class);
        serviceClasses.add(QueryResource.class);

        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();

        sf.setProviders(providers);
        sf.setResourceClasses(serviceClasses);
        sf.setAddress(HTTP_ENDPOINT_ADDRESS);
        server = sf.create();
    }

    @Test
    public void testGetSystemInfo() {
        final LinkedHashMap response = client
                .path("system/jvm")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(LinkedHashMap.class);
        assertTrue(client.get().getStatus() == Response.Status.OK.getStatusCode());
        assertTrue(response.get("Java vendor").equals(System.getProperty("java.vendor")));
    }

    @Test
    public void testGetProperties() {
        final LinkedHashMap response = client
                .path("system/properties")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(LinkedHashMap.class);
        assertTrue(client.get().getStatus() == Response.Status.OK.getStatusCode());
        assertTrue(response.values().size() > 0);
    }

    @Test
    public void testGetHardwareInfo() {
        final LinkedHashMap response = client
                .path("system/hardware")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(LinkedHashMap.class);
        assertTrue(client.get().getStatus() == Response.Status.OK.getStatusCode());
        assertTrue(response.get("OS architecture").equals(System.getProperty("os.arch")));
    }

    @Test
    public void testGetVersionInfo() {
        final String response = client
                .path("system/versions")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(String.class);
        assertTrue(client.get().getStatus() == Response.Status.OK.getStatusCode());
        assertTrue(response.contains("Repository vendor"));
    }

    @Test
    public void testGetVersionInfoWithCORS() throws IOException {
        final UsernamePasswordCredentials adminCredentials = new UsernamePasswordCredentials("admin", "admin");
        HttpClient httpclient = new HttpClient();
        httpclient.getParams().setAuthenticationPreemptive(true);
        httpclient.getState().setCredentials(AuthScope.ANY, adminCredentials);
        GetMethod getMethod = new GetMethod(HTTP_ENDPOINT_ADDRESS + "/system/versions");
        getMethod.addRequestHeader(CorsHeaderConstants.HEADER_ORIGIN, "http://somehost");
        getMethod.addRequestHeader("Accept", MediaType.APPLICATION_JSON);

        try {
            httpclient.executeMethod(getMethod);
        } finally {
            getMethod.releaseConnection();
        }

        assertTrue(getMethod.getResponseHeaders(CorsHeaderConstants.HEADER_AC_ALLOW_ORIGIN) != null);
        assertTrue(getMethod.getResponseHeader(CorsHeaderConstants.HEADER_AC_ALLOW_ORIGIN).getValue().equals("*"));
    }

    @Test
    public void testGetJcrRootNode() {
        final JcrNode response = client
                .path("nodes/")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(JcrNode.class);
        assertTrue(client.get().getStatus() == Response.Status.OK.getStatusCode());
        assertTrue(response.getName().equals(""));
        assertTrue(response.getPath().equals("/"));
        assertTrue(response.getPrimaryType().equals("rep:root"));
    }

    @Test
    public void testNotFoundForJcrNode() {
        final Response response = client
                .path("nodes/nonexistingnode")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(Response.class);
        assertTrue(response.getStatus() == Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetJcrRootNodeWithDepth() {
        final JcrNode response = client
                .path("nodes/")
                .query("depth", "1")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(JcrNode.class);
        assertTrue(response.getName().equals(""));
        assertTrue(response.getPath().equals("/"));
        assertTrue(response.getPrimaryType().equals("rep:root"));
        assertTrue(response.getNodes().size() == 4);
    }

    @Test
    public void testPostToJcrRootNode() {
        final ArrayList<JcrProperty> properties = new ArrayList<JcrProperty>(1);
        ArrayList<String> values;

        JcrNode node = new JcrNode();
        node.setName("newnode");
        node.setPrimaryType("nt:unstructured");

        final JcrProperty stringJcrProperty = new JcrProperty();
        stringJcrProperty.setName("myproperty");
        stringJcrProperty.setType("String");
        stringJcrProperty.setMultiple(false);
        values = new ArrayList<String>(1);
        values.add("test");
        stringJcrProperty.setValues(values);
        properties.add(stringJcrProperty);

        final JcrProperty multipleStringJcrProperty = new JcrProperty();
        multipleStringJcrProperty.setName("multiplestringproperty");
        multipleStringJcrProperty.setType("String");
        multipleStringJcrProperty.setMultiple(true);
        values = new ArrayList<String>(1);
        values.add("test1");
        values.add("test2");
        multipleStringJcrProperty.setValues(values);
        properties.add(multipleStringJcrProperty);

        final JcrProperty doubleJcrProperty = new JcrProperty();
        doubleJcrProperty.setName("mydoubleproperty");
        doubleJcrProperty.setType("Double");
        doubleJcrProperty.setMultiple(false);
        values = new ArrayList<String>(1);
        values.add("3.14");
        doubleJcrProperty.setValues(values);
        properties.add(doubleJcrProperty);

        final JcrProperty booleanJcrProperty = new JcrProperty();
        booleanJcrProperty.setName("myboolproperty");
        booleanJcrProperty.setType("Boolean");
        booleanJcrProperty.setMultiple(false);
        values = new ArrayList<String>(1);
        values.add("true");
        booleanJcrProperty.setValues(values);
        properties.add(booleanJcrProperty);
        node.setProperties(properties);

        final Response response = client
                .path("nodes/")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .post(node);
        assertTrue("Document was not created. Status code: " + response.getStatus() + " " + response,
                response.getStatus() == Response.Status.CREATED.getStatusCode());

        final Object location = response.getMetadata().getFirst("Location");
        assertTrue((HTTP_ENDPOINT_ADDRESS + "/nodes/newnode").equals(location));
        client.reset();
        final JcrNode newJcrNode = client
                .path("nodes/newnode")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(JcrNode.class);

        assertTrue(newJcrNode.getProperties().size()==4);
    }

    @Test
    public void testPostToNonExistingNode() {
        JcrNode node = new JcrNode();
        node.setName("newnode");
        node.setPrimaryType("nt:unstructured");

        final Response response = client
                .path("nodes/mynonexistingnode")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .post(node);
        assertTrue(response.getStatus() == Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testDeleteJcrNodeByPath() throws RepositoryException {
        session.getRootNode().addNode("test", "nt:unstructured");
        session.save();
        final Response response = client
                .path("nodes/test")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .delete();
        assertTrue(response.getStatus() == Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    public void testGetPropertyFromNode() {
        final JcrProperty response = client.path("properties/jcr:primaryType")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(JcrProperty.class);
        assertTrue(response != null);
        assertTrue(response.getName().equals("jcr:primaryType"));
        assertTrue(response.getValues().get(0).equals("rep:root"));
    }

    @Test
    public void testNotFoundOnGetProperty() {
        final Response response = client.path("properties/jcr:someProperty")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(Response.class);
        assertTrue(response.getStatus() == Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testAddProperty() throws RepositoryException {
        session.getRootNode().addNode("test", "nt:unstructured");
        session.save();

        final ArrayList<String> values = new ArrayList<String>(1);
        values.add("test");

        final JcrProperty jcrProperty = new JcrProperty();
        jcrProperty.setName("myproperty");
        jcrProperty.setType("String");
        jcrProperty.setMultiple(false);
        jcrProperty.setValues(values);

        final Response response = client
                .path("properties/test")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .post(jcrProperty);
        assertTrue(response.getStatus() == Response.Status.CREATED.getStatusCode());
        assertTrue(response.getMetadata().getFirst("Location").equals(HTTP_ENDPOINT_ADDRESS + "/properties/test/myproperty"));
        session.getRootNode().getNode("test").remove();
        session.save();
    }

    @Test
    public void testDeleteProperty() throws RepositoryException {
        final Node test = session.getRootNode().addNode("test", "nt:unstructured");
        test.setProperty("propname","propvalue");
        session.save();

        final Response emptyPathResponse = client
                .path("properties/")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .delete();

        assertTrue(emptyPathResponse.getStatus() == Response.Status.NOT_FOUND.getStatusCode());

        client.reset();

        final Response response = client
                .path("properties/test/propname")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .delete();
        assertTrue(response.getStatus() == Response.Status.NO_CONTENT.getStatusCode());
        assertFalse(session.getNode("/test").hasProperty("propname"));
        session.getNode("/test").remove();
        session.save();
    }

    @Test
    public void testGetQueryResults() {
        final JcrQueryResult response = client
                .path("query/")
                .query("statement", "//element(*,rep:root) order by @jcr:score")
                .query("language", "xpath")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(JcrQueryResult.class);
        assertTrue(response.getHits() == 1);
    }

    @Test
    public void testGetQueryResultsWithLimit() {
        final JcrQueryResult response = client
                .path("query/")
                .query("statement", "//element(*,hipposys:domain) order by @jcr:score")
                .query("language", "xpath")
                .query("limit", "1")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(JcrQueryResult.class);
        assertTrue(response.getNodes().size() == 1);
        assertTrue(response.getHits() > 1);
    }

    @Test
    public void testGetQueryResultsWithBody() {
        JcrSearchQuery query = new JcrSearchQuery();
        query.setStatement("SELECT * FROM rep:root order by jcr:score");
        query.setLanguage("sql");
        final JcrQueryResult response = client
                .path("query/")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .post(query, JcrQueryResult.class);
        assertTrue(response.getHits() == 1);
    }

    @After
    public void tearDown() throws Exception {
        tearDown(false);
    }

    @AfterClass
    public static void destroy() throws Exception {
        tearDownClass(true);
        if (server != null) {
            server.stop();
            server.destroy();
        }
    }

    private static String getDefaultRepoPath() {
        final File tmpdir = new File(System.getProperty("java.io.tmpdir"));
        final File storage = new File(tmpdir, "repository");
        if (!storage.exists()) {
            final boolean createdDir = storage.mkdir();
            if(!createdDir) {
                log.error("Something went wrong while trying to create the repository directory at: {}", storage.getAbsolutePath());
            }
        }
        return storage.getAbsolutePath();
    }

}
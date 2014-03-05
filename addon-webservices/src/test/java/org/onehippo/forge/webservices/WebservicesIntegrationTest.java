package org.onehippo.forge.webservices;

import java.io.File;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.client.WebClient;
import org.hippoecm.repository.HippoRepositoryFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onehippo.repository.testutils.RepositoryTestCase;

import static org.junit.Assert.assertTrue;

/**
 * Test case which runs an embedded Jetty and a Hippo Repository for testing the webservices.
 */
public class WebservicesIntegrationTest extends RepositoryTestCase {

    private final static String ENDPOINT_ADDRESS = "local:///";
    private final static String HTTP_ENDPOINT_ADDRESS = "http://localhost:8080/rest";
    private final static String DEFAULT_REPO_LOCATION = "file://";
    private static Server server;

    @BeforeClass
    public static void initialize() throws Exception {
        setUpClass();
        startServer();
    }

    @Before
    public void setUp() throws Exception {
        HippoRepositoryFactory.setDefaultRepository(DEFAULT_REPO_LOCATION + getDefaultRepoPath());
        setUp(false);
    }

    private static void startServer() throws Exception {
        RuntimeDelegate delegate = RuntimeDelegate.getInstance();
        JAXRSServerFactoryBean sf = delegate.createEndpoint(new HippoWebServicesApplication(), JAXRSServerFactoryBean.class);
        sf.setAddress(HTTP_ENDPOINT_ADDRESS);
        server = sf.create();
    }

    @Test
    public void testGetSystemInfo() {
        WebClient client = WebClient.create(HTTP_ENDPOINT_ADDRESS, "admin", "admin", null);
        final String response = client
                .path("v1/system/jvm")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(String.class);
        assertTrue(client.get().getStatus() == Response.Status.OK.getStatusCode());
        assertTrue(response.contains("Java vendor"));
    }

    @After
    public void tearDown() throws Exception {
        tearDown(false);
    }

    @AfterClass
    public static void destroy() throws Exception {
        tearDownClass();
        server.stop();
        server.destroy();
    }

    private static String getDefaultRepoPath() {
        final File tmpdir = new File(System.getProperty("java.io.tmpdir"));
        final File storage = new File(tmpdir, "repository");
        if (!storage.exists()) {
            storage.mkdir();
        }
        return storage.getAbsolutePath();
    }

}
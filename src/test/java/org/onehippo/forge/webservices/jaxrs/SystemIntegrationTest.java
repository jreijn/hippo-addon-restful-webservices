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

package org.onehippo.forge.webservices.jaxrs;

import java.io.IOException;
import java.util.LinkedHashMap;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.cxf.rs.security.cors.CorsHeaderConstants;
import org.junit.Test;
import org.onehippo.forge.webservices.WebservicesIntegrationTest;

import static org.junit.Assert.assertTrue;

public class SystemIntegrationTest extends WebservicesIntegrationTest {

    @Test
    public void testGetSystemInfo() {
        final LinkedHashMap response = client
                .path("v1/_system/jvm")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(LinkedHashMap.class);
        assertTrue(client.get().getStatus() == Response.Status.OK.getStatusCode());
        assertTrue(response.get("Java vendor").equals(System.getProperty("java.vendor")));
    }

    @Test
    public void testGetProperties() {
        final LinkedHashMap response = client
                .path("v1/_system/properties")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(LinkedHashMap.class);
        assertTrue(client.get().getStatus() == Response.Status.OK.getStatusCode());
        assertTrue(response.values().size() > 0);
    }

    @Test
    public void testGetHardwareInfo() {
        final LinkedHashMap response = client
                .path("v1/_system/hardware")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(LinkedHashMap.class);
        assertTrue(client.get().getStatus() == Response.Status.OK.getStatusCode());
        assertTrue(response.get("OS architecture").equals(System.getProperty("os.arch")));
    }

    @Test
    public void testGetVersionInfo() {
        final String response = client
                .path("v1/_system/versions")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(String.class);
        assertTrue(client.get().getStatus() == Response.Status.OK.getStatusCode());
        assertTrue(response.contains("Hippo Release Version"));
    }

    @Test
    public void testGetVersionInfoWithCORS() throws IOException {
        final UsernamePasswordCredentials adminCredentials = new UsernamePasswordCredentials("admin", "admin");
        HttpClient httpclient = new HttpClient();
        httpclient.getParams().setAuthenticationPreemptive(true);
        httpclient.getState().setCredentials(AuthScope.ANY, adminCredentials);
        GetMethod getMethod = new GetMethod(HTTP_ENDPOINT_ADDRESS + "/v1/_system/versions");
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

}

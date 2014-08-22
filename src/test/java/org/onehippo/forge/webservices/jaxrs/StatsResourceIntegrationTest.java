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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.onehippo.forge.webservices.WebservicesIntegrationTest;

import static org.junit.Assert.assertTrue;

public class StatsResourceIntegrationTest extends WebservicesIntegrationTest {

    @Test
    public void testGetInstanceInformation() throws Exception {
        final Response statistics = client
                .path("_stats")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(Response.class);
        assertTrue(client.get().getStatus() == Response.Status.OK.getStatusCode());
    }
}
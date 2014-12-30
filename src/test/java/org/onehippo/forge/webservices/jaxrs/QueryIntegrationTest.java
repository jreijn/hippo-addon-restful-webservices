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

import org.junit.Test;
import org.onehippo.forge.webservices.WebservicesIntegrationTest;
import org.onehippo.forge.webservices.jaxrs.jcr.model.JcrQueryResult;
import org.onehippo.forge.webservices.jaxrs.jcr.model.JcrSearchQuery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QueryIntegrationTest extends WebservicesIntegrationTest {

    @Test
    public void testGetQueryResults() {
        final String rootNodeLink = "http://localhost:8080/cms/rest/api/nodes/";
        final JcrQueryResult response = client
                .path("_query/")
                .query("statement", "//element(*,rep:root) order by @jcr:score")
                .query("language", "xpath")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(JcrQueryResult.class);
        assertTrue(response.getHits() == 1);
        assertEquals(rootNodeLink, response.getNodes().get(0).getLink().toString());
    }

    @Test
    public void testGetQueryResultsWithLimit() {
        final JcrQueryResult response = client
                .path("_query/")
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
                .path("_query/")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .post(query, JcrQueryResult.class);
        assertTrue(response.getHits() == 1);
    }

}

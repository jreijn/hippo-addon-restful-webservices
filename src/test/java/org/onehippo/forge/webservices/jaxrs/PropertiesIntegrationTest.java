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

import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.onehippo.forge.webservices.WebservicesIntegrationTest;
import org.onehippo.forge.webservices.jaxrs.jcr.model.JcrProperty;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PropertiesIntegrationTest extends WebservicesIntegrationTest {

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
    public void testAddPropertyNotFoundWithIncorrectPath() throws RepositoryException {
        final ArrayList<String> values = new ArrayList<String>(1);
        values.add("test");

        final JcrProperty jcrProperty = new JcrProperty();
        jcrProperty.setName("myproperty");
        jcrProperty.setType("String");
        jcrProperty.setMultiple(false);
        jcrProperty.setValues(values);

        final Response response = client
                .path("properties/test12355")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .post(jcrProperty);
        assertTrue(response.getStatus() == Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testAddPropertyFailsWithIncorrectInput() throws RepositoryException {
        session.getRootNode().addNode("test", "nt:unstructured");
        session.save();

        final ArrayList<String> values = new ArrayList<String>(1);
        values.add("test");

        final JcrProperty jcrProperty = new JcrProperty();
        jcrProperty.setName("");
        jcrProperty.setType("String");
        jcrProperty.setMultiple(false);
        jcrProperty.setValues(values);

        final Response response = client
                .path("properties/test")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .post(jcrProperty);
        assertTrue(response.getStatus() == Response.Status.BAD_REQUEST.getStatusCode());

        client.reset();

        jcrProperty.setName("name");
        jcrProperty.setType("");

        final Response typedResponse = client
                .path("properties/test")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .post(jcrProperty);
        assertTrue(typedResponse.getStatus() == Response.Status.BAD_REQUEST.getStatusCode());

        session.getRootNode().getNode("test").remove();
        session.save();
    }

    @Test
    public void testUpdateProperty() throws RepositoryException {
        session.getRootNode().addNode("test", "nt:unstructured");
        session.save();

        final ArrayList<String> values = new ArrayList<String>(1);
        values.add("test");

        final JcrProperty jcrProperty = new JcrProperty();
        jcrProperty.setName("test");
        jcrProperty.setType("String");
        jcrProperty.setMultiple(false);
        jcrProperty.setValues(values);

        final Response response = client
                .path("properties/test")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .post(jcrProperty);
        assertTrue(response.getStatus() == Response.Status.CREATED.getStatusCode());

        client.reset();

        values.remove("test");
        values.add("test2");
        final Response updateResponse = client
                .path("properties/test/test")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .put(jcrProperty);
        assertTrue(updateResponse.getStatus() == Response.Status.NO_CONTENT.getStatusCode());

        client.reset();
        final JcrProperty newValuedProperty = client.path("properties/test/test")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(JcrProperty.class);

        assertTrue(newValuedProperty.getValues().get(0).equals("test2"));

        session.getRootNode().getNode("test").remove();
        session.save();

    }

    @Test
    public void testDeleteProperty() throws RepositoryException {
        final Node test = session.getRootNode().addNode("test", "nt:unstructured");
        test.setProperty("propname", "propvalue");
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


}

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

import javax.jcr.RepositoryException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.onehippo.forge.webservices.WebservicesIntegrationTest;
import org.onehippo.forge.webservices.jaxrs.jcr.model.JcrNode;
import org.onehippo.forge.webservices.jaxrs.jcr.model.JcrProperty;

import static org.junit.Assert.assertTrue;

public class NodesIntegrationTest extends WebservicesIntegrationTest {

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
    public void testPostToJcrRootNode() throws RepositoryException {
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

        assertTrue("Expected 4 properties, but received: "+ newJcrNode.getProperties().size(),  newJcrNode.getProperties().size() == 4);

        // cleanup
        session.getRootNode().getNode("newnode").remove();
        session.save();
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
    public void testDeleteNonExistingJcrNodeByPath() throws RepositoryException {
        final Response response = client
                .path("nodes/nonexistingnode")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .delete();
        assertTrue(response.getStatus() == Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testDeleteWithEmptyPath() throws RepositoryException {
        final Response response = client
                .path("nodes/")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .delete();
        assertTrue(response.getStatus() == Response.Status.NOT_FOUND.getStatusCode());
    }

}

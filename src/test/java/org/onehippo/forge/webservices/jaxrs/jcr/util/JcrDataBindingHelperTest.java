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

package org.onehippo.forge.webservices.jaxrs.jcr.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NodeType;

import org.apache.sling.commons.testing.jcr.MockValue;
import org.junit.Test;
import org.onehippo.forge.webservices.jaxrs.jcr.model.JcrNode;
import org.onehippo.forge.webservices.jaxrs.jcr.model.JcrProperty;
import org.onehippo.forge.webservices.testing.jcr.MockNode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link org.onehippo.forge.webservices.jaxrs.jcr.util.JcrDataBindingHelper}
 */
public class JcrDataBindingHelperTest {

    private static final String MIX_VERSIONABLE = "mix:versionable";
    private static final String HIPPO_PATHS_PROPERTY = "hippo:paths";

    @Test
    public void testGetNodeRepresentation() throws Exception {

        MockNode mockRootNode = new MockNode("/", "jcr:root");
        final Node testNode = mockRootNode.addNode("/test", "hippo:handle");
        testNode.addNode("/test/test1", "hippo:document");
        testNode.addNode("/test/test2", "hippo:document");

        final JcrNode nodeRepresentation = JcrDataBindingHelper.getNodeRepresentation(mockRootNode.getNode("test"), 2);
        assertTrue(nodeRepresentation.getName().equals("test"));
        assertEquals("hippo:handle", nodeRepresentation.getPrimaryType());
        assertEquals("/test", nodeRepresentation.getPath());
        assertEquals(2, nodeRepresentation.getNodes().size());

    }

    @Test
    public void testGetPropertyRepresentation() throws Exception {
        MockNode mockRootNode = new MockNode("/", "jcr:root");
        MockNode testNode = (MockNode) mockRootNode.addNode("/test", "hippo:document");
        testNode.setProperty("hippostdpubwf:createdBy", "admin");
        testNode.setProperty(HIPPO_PATHS_PROPERTY, new String[]{"test", "test2"});
        JcrProperty propertyRepresentation = JcrDataBindingHelper.getPropertyRepresentation(mockRootNode.getNode("test").getProperty("hippostdpubwf:createdBy"));
        assertEquals(propertyRepresentation.getName(), "hippostdpubwf:createdBy");
        JcrProperty pathsPropertyRepresentation = JcrDataBindingHelper.getPropertyRepresentation(mockRootNode.getNode("test").getProperty(HIPPO_PATHS_PROPERTY));
        assertTrue(pathsPropertyRepresentation.isMultiple());
    }

    @Test
    public void testAddMixinToNode() throws Exception {
        List<String> mixins = new ArrayList<String>();
        mixins.add(MIX_VERSIONABLE);

        MockNode rootNode = new MockNode("/", "jcr:root");
        Node testNode = rootNode.addNode("/test", "hippo:document");
        testNode.setProperty("hippostdpubwf:createdBy", "admin");
        JcrDataBindingHelper.addMixinsFromRepresentation(rootNode, mixins);
        NodeType[] mixinNodeTypes = rootNode.getMixinNodeTypes();
        boolean mixinFound = false;
        if (mixinNodeTypes != null) {
            for (NodeType type : mixinNodeTypes) {
                if (type.getName().equals(MIX_VERSIONABLE)) {
                    mixinFound = true;
                }
            }
        }
        assertTrue(mixinFound);
    }

    @Test
    public void testAddPropertyToNode() throws IOException, RepositoryException {
        List<String> propValues = new ArrayList<String>();
        propValues.add("value1");

        MockNode mockRootNode = new MockNode("/", "jcr:root");
        Session mockSession = mock(Session.class);
        mockRootNode.setSession(mockSession);
        Node testNode = mockRootNode.addNode("/test", "hippo:document");
        testNode.setProperty("hippostdpubwf:createdBy", "admin");

        JcrProperty jcrProperty = new JcrProperty();
        jcrProperty.setName("propname");
        jcrProperty.setValues(propValues);
        jcrProperty.setType("String");
        jcrProperty.setMultiple(false);
        final ValueFactory valueFactory = mock(ValueFactory.class);
        when(mockRootNode.getSession().getValueFactory()).thenReturn(valueFactory);
        when(valueFactory.createValue("value1", 1)).thenReturn(new MockValue("value1"));
        JcrDataBindingHelper.addPropertyToNode(mockRootNode, jcrProperty);
        assertTrue(mockRootNode.hasProperty("propname"));
    }

    @Test
    public void testAddChildNodesFromRepresentation() throws Exception {
        List<JcrNode> nodes = new ArrayList<JcrNode>(2);
        final JcrNode node1 = new JcrNode();
        node1.setName("test1");
        node1.setPrimaryType("hippo:document");
        final JcrNode subnode1 = new JcrNode();
        subnode1.setName("subtest1");
        subnode1.setPrimaryType("hippo:compound");
        node1.addNode(subnode1);
        nodes.add(node1);

        final JcrNode node2 = new JcrNode();
        node2.setName("test2");
        node1.setPrimaryType("hippo:document");
        nodes.add(node2);
        MockNode testMock = new MockNode("/", "jcr:root");
        JcrDataBindingHelper.addChildNodesFromRepresentation(testMock, nodes);
        assertEquals(2, testMock.getNodes().getSize());
    }
}

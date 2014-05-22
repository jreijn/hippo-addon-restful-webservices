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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NodeType;

import org.apache.jackrabbit.value.ValueHelper;
import org.onehippo.forge.webservices.jaxrs.jcr.model.JcrNode;
import org.onehippo.forge.webservices.jaxrs.jcr.model.JcrProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for mapping to and from JCR.
 *
 * @author jreijn
 */
public final class JcrDataBindingHelper {

    private static Logger log = LoggerFactory.getLogger(JcrDataBindingHelper.class);

    private static final Map<Integer, String> HIPPO_PROPERTY_BLACKLIST = createMap();

    private static Map<Integer, String> createMap() {
        Map<Integer, String> result = new HashMap<Integer, String>();
        result.put(1, "hippo:paths");
        result.put(2, "hippo:related");
        return Collections.unmodifiableMap(result);
    }

    private JcrDataBindingHelper() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the representation of a JCR node.
     *
     * @param node  the {@link javax.jcr.Node}
     * @param depth the amount of children to fetch underneath this node
     * @return a representation of a JCR node
     */
    public static JcrNode getNodeRepresentation(final Node node, final int depth) {
        JcrNode jcrNode = new JcrNode();
        try {
            jcrNode.setName(node.getName());
            jcrNode.setIdentifier(node.getIdentifier());
            jcrNode.setPath(node.getPath());
            jcrNode.setPrimaryType(node.getPrimaryNodeType().getName());

            final NodeType[] mixinNodeTypes = node.getMixinNodeTypes();
            if (mixinNodeTypes != null) {
                List<String> mixins = new ArrayList<String>(mixinNodeTypes.length);
                for (NodeType mixinNodeType : mixinNodeTypes) {
                    mixins.add(mixinNodeType.getName());
                }
                jcrNode.setMixinTypes(mixins);
            }

            PropertyIterator properties = node.getProperties();
            while (properties.hasNext()) {
                Property property = properties.nextProperty();
                if (!property.getName().startsWith("jcr:")) {
                    jcrNode.getProperties().add(getPropertyRepresentation(property));
                }
            }

            if (depth > 0 && node.hasNodes()) {
                final NodeIterator childNodes = node.getNodes();
                while (childNodes.hasNext()) {
                    jcrNode.addNode(getNodeRepresentation(childNodes.nextNode(), depth - 1));
                }
            }
        } catch (RepositoryException e) {
            log.error("An exception occurred while trying to marshall node: {} ", e);
        }
        return jcrNode;
    }

    public static JcrProperty getPropertyRepresentation(Property property) throws RepositoryException {
        JcrProperty data = new JcrProperty();
        data.setName(property.getName());
        data.setType(PropertyType.nameFromValue(property.getType()));
        data.setMultiple(property.isMultiple());

        List<String> values = new ArrayList<String>();
        if (property.isMultiple()) {
            for (Value propertyValue : property.getValues()) {
                values.add(propertyValue.getString());
            }
        } else {
            values.add(property.getValue().getString());
        }
        data.setValues(values);
        return data;
    }

    /**
     * Parses the list of mixins and applies them to the {@link Node}
     *
     * @param node
     * @param mixins
     * @throws RepositoryException
     */
    public static void addMixinsFromRepresentation(final Node node, final List<String> mixins) throws RepositoryException {
        for (String mixin : mixins) {
            if (node.canAddMixin(mixin)) {
                node.addMixin(mixin);
            }
        }
    }

    public static void addPropertiesFromRepresentation(final Node node, final List<JcrProperty> jcrProperties) throws RepositoryException {
        for (JcrProperty property : jcrProperties) {
            if (HIPPO_PROPERTY_BLACKLIST.containsValue(property.getName())) {
                continue;
            }
            addPropertyToNode(node, property);
        }
    }

    public static void addChildNodesFromRepresentation(final Node node, List<JcrNode> nodes) throws RepositoryException {
        for (JcrNode jcrNode : nodes) {
            Node childNode = node.addNode(jcrNode.getName(), jcrNode.getPrimaryType());
            addPropertiesFromRepresentation(childNode, jcrNode.getProperties());
            addMixinsFromRepresentation(childNode, jcrNode.getMixinTypes());
            if (!jcrNode.getNodes().isEmpty()) {
                addChildNodesFromRepresentation(childNode, jcrNode.getNodes());
            }
        }
    }

    public static void addPropertyToNode(final Node node, final JcrProperty property) throws RepositoryException {
        ValueFactory valueFactory = node.getSession().getValueFactory();
        if (property.isMultiple()) {
            Value[] values;
            if (property.getValues() != null) {
                values = new Value[property.getValues().size()];
                int propertyType = PropertyType.valueFromName(property.getType());
                int i = 0;
                for (String propertyValue : property.getValues()) {
                    values[i++] = ValueHelper.convert(propertyValue, propertyType, valueFactory);
                }
            } else {
                values = new Value[0];
            }

            String propName = property.getName();
            if (node.hasProperty(propName)) {
                Property existingProperty = node.getProperty(propName);
                if (!existingProperty.isMultiple()) {
                    existingProperty.remove();
                }
            }
            node.setProperty(property.getName(), values);
        } else {
            final int propertyType = PropertyType.valueFromName(property.getType());
            final String propertyValue = property.getValues().get(0);

            Value value = ValueHelper.convert(propertyValue, propertyType, valueFactory);
            String name = property.getName();
            if (node.hasProperty(name)) {
                Property existingProperty = node.getProperty(name);
                if (existingProperty.isMultiple()) {
                    existingProperty.remove();
                }
            }
            node.setProperty(property.getName(), value);
        }
    }

}

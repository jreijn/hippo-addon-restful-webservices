package org.onehippo.forge.webservices.testing.jcr;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.commons.testing.jcr.MockNodeIterator;
import org.apache.sling.commons.testing.jcr.MockNodeType;

/**
 * Simple implementation supporthing child nodes.
 * Created by jreijn on 14/04/14.
 */
public class MockNode extends org.apache.sling.commons.testing.jcr.MockNode {

    private List<MockNode> childNodes = new ArrayList<MockNode>();
    private List<MockNodeType> mixins = new ArrayList<MockNodeType>();

    private MockNode parent;

    public MockNode(final String path) {
        super(path);
    }

    public MockNode(final String path, final String type) {
        super(path, type);
    }

    @Override
    public Node getNode(final String relPath) {
        for(MockNode node : childNodes) {
            final String childNodeName = node.getPath().substring(node.getPath().lastIndexOf('/') + 1);
            if(childNodeName.equals(relPath)) {
                return node;
            }
        }
        return null;
    }

    @Override
    public boolean hasNodes() {
        return !childNodes.isEmpty();
    }

    @Override
    public NodeIterator getNodes() {
        return new MockNodeIterator(childNodes.toArray(new Node[childNodes.size()]));
    }

    @Override
    public Node addNode(final String relPath, final String primaryNodeTypeName) {
        final MockNode mockNode = new MockNode(relPath, primaryNodeTypeName);
        mockNode.setParent(this);
        childNodes.add(mockNode);
        return mockNode;
    }

    @Override
    public void addMixin(final String mixinName) {
        mixins.add(new MockNodeType(mixinName));
    }

    @Override
    public boolean canAddMixin(final String mixinName) {
        return true;
    }

    @Override
    public NodeType[] getMixinNodeTypes() {
        return mixins.toArray(new NodeType [mixins.size()]);
    }

    public MockNode getParent() {
        return parent;
    }

    public void setParent(final MockNode parent) {
        this.parent = parent;
    }

}

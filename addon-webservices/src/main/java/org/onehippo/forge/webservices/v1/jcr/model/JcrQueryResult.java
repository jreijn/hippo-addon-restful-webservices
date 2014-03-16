package org.onehippo.forge.webservices.v1.jcr.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Jeroen Reijn
 */
@XmlRootElement(name="result")
public class JcrQueryResult {

    private List<JcrNode> nodes;
    private long hits;

    public long getHits() {
        return hits;
    }

    public void setHits(final long hits) {
        this.hits = hits;
    }

    public List<JcrNode> getNodes() {
        return nodes;
    }

    public void setNodes(final List<JcrNode> nodes) {
        this.nodes = nodes;
    }
}

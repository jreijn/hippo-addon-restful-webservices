package org.onehippo.forge.webservices.v1.jcr;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Jeroen Reijn
 */
@XmlRootElement(name="result")
public class JcrQueryResult {

    private List<JcrNode> items;
    private String type;
    private String query;

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(final String query) {
        this.query = query;
    }

    public List<JcrNode> getItems() {
        return items;
    }

    public void setItems(final List<JcrNode> items) {
        this.items = items;
    }
}

package org.onehippo.forge.webservices.v1.system;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Jeroen Reijn
 */
@XmlRootElement(name = "property")
public class SystemProperty {

    private String name;
    private String value;

    public SystemProperty() {
    }

    public SystemProperty(final String name, final String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }
}

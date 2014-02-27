package org.onehippo.forge.webservices.v1.system;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Jeroen Reijn
 */
@XmlRootElement(name = "system")
public class SystemInfo {
    private List<SystemProperty> properties = new ArrayList<SystemProperty>();

    public List<SystemProperty> getProperties() {
        return properties;
    }

    public void setProperties(final List<SystemProperty> properties) {
        this.properties = properties;
    }

    public void addProperty(SystemProperty property) {
        this.properties.add(property);
    }
}

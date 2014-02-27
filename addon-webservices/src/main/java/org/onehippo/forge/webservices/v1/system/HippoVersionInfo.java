package org.onehippo.forge.webservices.v1.system;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Jeroen Reijn
 */
@XmlRootElement(name="versions")
public class HippoVersionInfo {

    private String cmsVersion;
    private String repositoryVersion;

    @XmlElement(name = "Hippo-CMS-version")
    public String getCmsVersion() {
        return cmsVersion;
    }

    public void setCmsVersion(final String cmsVersion) {
        this.cmsVersion = cmsVersion;
    }

    @XmlElement(name = "Hippo-Repository-version")
    public String getRepositoryVersion() {
        return repositoryVersion;
    }

    public void setRepositoryVersion(final String repositoryVersion) {
        this.repositoryVersion = repositoryVersion;
    }
}

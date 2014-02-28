package org.onehippo.forge.webservices.beans;

import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoHtml;

@Node(jcrType="hippowebservices:textdocument")
public class TextDocument extends BaseDocument{
    
    public String getTitle() {
        return getProperty("hippowebservices:title");
    }

    public String getSummary() {
        return getProperty("hippowebservices:summary");
    }
    
    public HippoHtml getHtml(){
        return getHippoHtml("hippowebservices:body");
    }

}

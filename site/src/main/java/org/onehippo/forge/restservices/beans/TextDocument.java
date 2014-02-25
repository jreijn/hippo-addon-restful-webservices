package org.onehippo.forge.restservices.beans;

import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoHtml;

@Node(jcrType="hipporestservices:textdocument")
public class TextDocument extends BaseDocument{
    
    public String getTitle() {
        return getProperty("hipporestservices:title");
    }

    public String getSummary() {
        return getProperty("hipporestservices:summary");
    }
    
    public HippoHtml getHtml(){
        return getHippoHtml("hipporestservices:body");    
    }

}

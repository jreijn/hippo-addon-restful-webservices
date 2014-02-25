package org.onehippo.forge.restservices.beans;

import java.util.Calendar;

import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoHtml;
import org.hippoecm.hst.content.beans.standard.HippoGalleryImageSetBean;

@Node(jcrType="hipporestservices:newsdocument")
public class NewsDocument extends BaseDocument{

    public String getTitle() {
        return getProperty("hipporestservices:title");
    }
    
    public String getSummary() {
        return getProperty("hipporestservices:summary");
    }
    
    public Calendar getDate() {
        return getProperty("hipporestservices:date");
    }

    public HippoHtml getHtml(){
        return getHippoHtml("hipporestservices:body");    
    }

    /**
     * Get the imageset of the newspage
     *
     * @return the imageset of the newspage
     */
    public HippoGalleryImageSetBean getImage() {
        return getLinkedBean("hipporestservices:image", HippoGalleryImageSetBean.class);
    }


}

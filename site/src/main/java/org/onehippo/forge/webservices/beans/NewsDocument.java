package org.onehippo.forge.webservices.beans;

import java.util.Calendar;

import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoHtml;
import org.hippoecm.hst.content.beans.standard.HippoGalleryImageSetBean;

@Node(jcrType="hippowebservices:newsdocument")
public class NewsDocument extends BaseDocument{

    public String getTitle() {
        return getProperty("hippowebservices:title");
    }
    
    public String getSummary() {
        return getProperty("hippowebservices:summary");
    }
    
    public Calendar getDate() {
        return getProperty("hippowebservices:date");
    }

    public HippoHtml getHtml(){
        return getHippoHtml("hippowebservices:body");
    }

    /**
     * Get the imageset of the newspage
     *
     * @return the imageset of the newspage
     */
    public HippoGalleryImageSetBean getImage() {
        return getLinkedBean("hippowebservices:image", HippoGalleryImageSetBean.class);
    }


}

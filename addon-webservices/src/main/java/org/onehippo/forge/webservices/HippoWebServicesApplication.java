package org.onehippo.forge.webservices;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.jaxrs.listing.ApiDeclarationProvider;
import com.wordnik.swagger.jaxrs.listing.ApiListingResourceJSON;
import com.wordnik.swagger.jaxrs.listing.ResourceListingProvider;

import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharingFilter;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.onehippo.forge.webservices.jaxrs.exception.CustomWebApplicationExceptionMapper;
import org.onehippo.forge.webservices.v1.jcr.NodesResource;
import org.onehippo.forge.webservices.v1.jcr.PropertyResource;
import org.onehippo.forge.webservices.v1.jcr.QueryResource;
import org.onehippo.forge.webservices.v1.system.SystemResource;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

/**
 * Application that bootstraps the services
 */
public class HippoWebServicesApplication extends Application {

    private Set<Class<?>> classes = new HashSet<Class<?>>();

    public HippoWebServicesApplication() {
        classes.add(SystemResource.class);
        classes.add(NodesResource.class);
        classes.add(QueryResource.class);
        classes.add(PropertyResource.class);
        classes.add(ApiListingResourceJSON.class);
        classes.add(ApiDeclarationProvider.class);
        classes.add(JacksonJaxbJsonProvider.class);
        classes.add(ResourceListingProvider.class);
        classes.add(CustomWebApplicationExceptionMapper.class);
        classes.add(CrossOriginResourceSharingFilter.class);
        classes.add(HippoAuthenticationRequestHandler.class);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }

}

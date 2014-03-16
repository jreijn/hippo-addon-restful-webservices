package org.onehippo.forge.webservices;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.jaxrs.listing.ApiDeclarationProvider;
import com.wordnik.swagger.jaxrs.listing.ApiListingResourceJSON;
import com.wordnik.swagger.jaxrs.listing.ResourceListingProvider;

import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.onehippo.forge.webservices.jaxrs.exception.CustomWebApplicationExceptionMapper;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

/**
 * Application that bootstraps the services
 */
public class HippoWebServicesApplication extends Application {

    private String resourcePackage = "org.onehippo.forge";
    private Set<Class<?>> classes = new HashSet<Class<?>>();

    public HippoWebServicesApplication() {
        final ConfigurationBuilder config = new ConfigurationBuilder();
        config.setUrls(ClasspathHelper.forPackage(this.resourcePackage)).setScanners(
                new TypeAnnotationsScanner(), new SubTypesScanner());

        classes = new Reflections(config).getTypesAnnotatedWith(Api.class);
        classes.add(ApiListingResourceJSON.class);
        classes.add(ApiDeclarationProvider.class);
        classes.add(JacksonJaxbJsonProvider.class);
        classes.add(ResourceListingProvider.class);
        classes.add(CustomWebApplicationExceptionMapper.class);
        classes.add(HippoAuthenticationRequestHandler.class);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }

}

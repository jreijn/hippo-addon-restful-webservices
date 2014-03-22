package org.onehippo.forge.webservices.swagger;

import javax.servlet.ServletConfig;

import com.wordnik.swagger.config.ScannerFactory;
import com.wordnik.swagger.jaxrs.config.DefaultJaxrsConfig;
import com.wordnik.swagger.jaxrs.config.ReflectiveJaxrsScanner;

/**
 * Custom implementation of the DefaultJaxrsConfig,
 * so we can use the reflective scanner instead of using an CXF application.
 * @author jreijn
 */
public class ReflectiveJaxrsConfig extends DefaultJaxrsConfig {

    @Override
    public void init(final ServletConfig servletConfig) {
        super.init(servletConfig);
        final ReflectiveJaxrsScanner reflectiveJaxrsScanner = new ReflectiveJaxrsScanner();
        reflectiveJaxrsScanner.setResourcePackage("org.onehippo.forge.webservices.v1");
        ScannerFactory.setScanner(reflectiveJaxrsScanner);
    }
}

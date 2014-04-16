package org.onehippo.forge.webservices.swagger;

import javax.servlet.ServletConfig;

import com.wordnik.swagger.config.ScannerFactory;
import com.wordnik.swagger.jaxrs.config.DefaultJaxrsConfig;
import com.wordnik.swagger.jaxrs.config.ReflectiveJaxrsScanner;

import org.datanucleus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom implementation of the DefaultJaxrsConfig,
 * so we can use the reflective scanner instead of using an CXF application.
 * @author jreijn
 */
public class ReflectiveJaxrsConfig extends DefaultJaxrsConfig {

    private static Logger log = LoggerFactory.getLogger(ReflectiveJaxrsConfig.class);

    @Override
    public void init(final ServletConfig servletConfig) {
        super.init(servletConfig);
        final ReflectiveJaxrsScanner reflectiveJaxrsScanner = new ReflectiveJaxrsScanner();
        final String resourcePackageParam = servletConfig.getInitParameter("resourcePackage");
        if(StringUtils.isEmpty(resourcePackageParam)) {
            log.warn("Please specify the resourcePackage init parameter and point it to the package of your webservice resources");
        }
        reflectiveJaxrsScanner.setResourcePackage(resourcePackageParam);
        ScannerFactory.setScanner(reflectiveJaxrsScanner);
    }
}

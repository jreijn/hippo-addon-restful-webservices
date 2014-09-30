/*
 * Copyright 2014 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onehippo.forge.webservices.swagger;

import javax.servlet.ServletConfig;

import com.wordnik.swagger.config.ScannerFactory;
import com.wordnik.swagger.jaxrs.config.DefaultJaxrsConfig;
import com.wordnik.swagger.jaxrs.config.ReflectiveJaxrsScanner;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom implementation of the DefaultJaxrsConfig,
 * so we can use the reflective scanner instead of using an CXF application.
 */
public class ReflectiveJaxrsConfig extends DefaultJaxrsConfig {

    private static final Logger log = LoggerFactory.getLogger(ReflectiveJaxrsConfig.class);

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

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

package org.onehippo.forge.webservices.jaxrs.system;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.jcr.LoginException;
import javax.jcr.Session;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.onehippo.forge.webservices.jaxrs.jcr.util.RepositoryConnectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Api for system information
 *
 * @author Jeroen Reijn
 */
@Api(value = "_system", description = "API for system information", position = 6)
@Path(value = "_system")
@CrossOriginResourceSharing(allowAllOrigins = true)
public class SystemResource {

    private static Logger log = LoggerFactory.getLogger(SystemResource.class);
    private static final double MB = 1024 * 1024;

    @Context
    private ServletContext servletContext;

    @Context
    private HttpServletRequest request;

    @ApiOperation(
            value = "Display the system properties",
            notes = "",
            position = 1)
    @Path(value = "/properties")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getSystemInfo() {
        Properties properties = System.getProperties();
        return Response.ok(properties).build();
    }

    @ApiOperation(
            value = "Display the version information",
            notes = "",
            position = 2)
    @Path(value = "/versions")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVersionInfo() {
        Session session = null;
        Map<String, String> info = new LinkedHashMap<String, String>();
        try {
            session = RepositoryConnectionUtils.createSession(request);
            info.put("Hippo Release Version", getHippoReleaseVersion());
            info.put("Project Version", getProjectVersion());
        } catch (LoginException e) {
            log.warn("An exception occurred while trying to login: {}", e);
        } finally {
            RepositoryConnectionUtils.cleanupSession(session);
        }

        return Response.ok(info).build();
    }

    @ApiOperation(
            value = "Display the hardware information",
            notes = "",
            position = 3)
    @Path(value = "/hardware")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHardwareInfo() {
        Map<String, String> info = new LinkedHashMap<String, String>();
        Runtime runtime = Runtime.getRuntime();
        info.put("OS architecture", System.getProperty("os.arch"));
        info.put("OS name", System.getProperty("os.name"));
        info.put("OS version", System.getProperty("os.version"));
        info.put("Processors", "# " + runtime.availableProcessors());
        return Response.ok(info).build();
    }

    @ApiOperation(
            value = "Display the memory information from the JVM",
            notes = "",
            position = 4)
    @Path(value = "/jvm")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMemoryInfo() {
        Map<String, String> info = new LinkedHashMap<String, String>();
        Runtime runtime = Runtime.getRuntime();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        info.put("Java vendor", System.getProperty("java.vendor"));
        info.put("Java version", System.getProperty("java.version"));
        info.put("Java VM", System.getProperty("java.vm.name"));
        info.put("Memory maximum", nf.format(((double) runtime.maxMemory()) / MB) + " MB");
        info.put("Memory taken", nf.format(((double) runtime.totalMemory()) / MB) + " MB");
        info.put("Memory free", nf.format(((double) runtime.freeMemory()) / MB) + " MB");
        info.put("Memory in use", nf.format(((double) (runtime.totalMemory() - runtime.freeMemory())) / MB) + " MB");
        info.put("Memory total free", nf.format(((double)
                (runtime.maxMemory() - runtime.totalMemory() + runtime.freeMemory())) / MB) + " MB");
        return Response.ok(info).build();
    }

    private String getHippoReleaseVersion() {
        try {
            final Manifest manifest = getWebAppManifest();
            if (manifest != null) {
                return manifest.getMainAttributes().getValue("Hippo-Release-Version");
            }
        } catch (IOException iOException) {
            log.debug("Error occurred getting the hippo cms release version from the webapp-manifest.", iOException);
        }
        return "unknown";
    }

    private String getProjectVersion() {
        try {
            final Manifest manifest = getWebAppManifest();
            if (manifest != null) {
                return buildVersionString(manifest, "Project-Version", "Project-Build");
            }
        } catch (IOException iOException) {
            log.debug("Error occurred getting the project version from the webapp-manifest.", iOException);
        }
        return "unknown";
    }

    private Manifest getWebAppManifest() throws IOException {
        final InputStream manifestInputStream = servletContext.getResourceAsStream("META-INF/MANIFEST.MF");
        if (manifestInputStream != null) {
            return new Manifest(manifestInputStream);
        }

        final File manifestFile = new File(servletContext.getRealPath("/"), "META-INF/MANIFEST.MF");
        if (manifestFile.exists()) {
            return new Manifest(new FileInputStream(manifestFile));
        }
        return null;
    }

    private String buildVersionString(final Manifest manifest, final String versionAttribute, final String buildAttribute) {
        StringBuilder versionString = new StringBuilder();

        final Attributes attributes = manifest.getMainAttributes();
        final String projectVersion = attributes.getValue(versionAttribute);
        if (projectVersion != null) {
            versionString.append(projectVersion);
        }
        final String projectBuild = attributes.getValue(buildAttribute);
        if (projectBuild != null && !"-1".equals(projectBuild)) {
            if (versionString.length() > 0) {
                versionString.append(", build: ");
            }
            versionString.append(projectBuild);
        }
        return versionString.toString();
    }

}

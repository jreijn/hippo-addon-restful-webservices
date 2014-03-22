package org.onehippo.forge.webservices.v1.system;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.jcr.LoginException;
import javax.jcr.Repository;
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

import org.apache.commons.io.IOUtils;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.hippoecm.frontend.Home;
import org.onehippo.forge.webservices.v1.jcr.util.RepositoryConnectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Api for system information
 * @author Jeroen Reijn
 */
@Api(value = "v1/system", description = "API for system information")
@Path(value = "v1/system")
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
            info.put("Hippo CMS version", getCMSVersion());
            info.put("Project Version", getProjectVersion());
            info.put("Repository vendor", getRepositoryVendor(session));
            info.put("Repository version", getRepositoryVersion(session));
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

    @ApiOperation(
            value = "Display jackrabbit information",
            notes = "",
            position = 5)
    @Path(value = "/jackrabbit")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJackrabbitInfo() {
        Session session = null;
        Map<String, String> info = new LinkedHashMap<String, String>();
        try {
            session = RepositoryConnectionUtils.createSession(request);
            info.put("Jackrabbit Cluster Node id", getClusterNodeId(session));
        } catch (LoginException e) {
            log.warn("An exception occurred while trying to login: {}", e);
        } finally {
            RepositoryConnectionUtils.cleanupSession(session);
        }
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

    private String getRepositoryVersion(Session session) {
        Repository repository = session.getRepository();
        if (repository != null) {
            return repository.getDescriptor(Repository.REP_VERSION_DESC);
        } else {
            return "unknown";
        }
    }

    private String getRepositoryVendor(Session session) {
        Repository repository = session.getRepository();
        if (repository != null) {
            return repository.getDescriptor(Repository.REP_NAME_DESC);
        } else {
            return "unknown";
        }
    }

    private String getCMSVersion() {
        try {
            Manifest manifest = getManifest(Home.class);
            if (manifest == null) {
                manifest = getWebAppManifest();
            }
            if (manifest != null) {
                return buildVersionString(manifest, "Implementation-Version", "Implementation-Build");
            }
        } catch (IOException iOException) {
            log.debug("Error occurred getting the cms version from the manifest.", iOException);
        }

        return "unknown";
    }

    /**
     * @param clazz the class object for which to obtain a reference to the manifest
     * @return the URL of the manifest found, or {@code null} if it could not be obtained
     */
    private static URL getManifestURL(Class clazz) {
        try {
            final StringBuilder sb = new StringBuilder();
            final String[] classElements = clazz.getName().split("\\.");
            for (int i = 0; i < classElements.length - 1; i++) {
                sb.append("../");
            }
            sb.append("META-INF/MANIFEST.MF");
            final URL classResource = clazz.getResource(classElements[classElements.length - 1] + ".class");
            if (classResource != null) {
                return new URL(classResource, new String(sb));
            }
        } catch (MalformedURLException exception) {
            log.warn("Invalid URL detected while trying to get the URl for the Manifest: {}", exception);
        }
        return null;
    }

    /**
     * @param clazz the class object for which to obtain the manifest
     * @return the manifest object, or {@code null} if it could not be obtained
     * @throws IOException if something went wrong while reading the manifest
     */
    private static Manifest getManifest(Class clazz) throws IOException {
        final URL url = getManifestURL(clazz);
        if (url != null) {
            final InputStream is = url.openStream();
            try {
                return new Manifest(is);
            } finally {
                IOUtils.closeQuietly(is);
            }
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

    private String getClusterNodeId(Session session) {
        String clusterNodeId = "";
        if (session.getRepository() != null) {
            final String clusterId = session.getRepository().getDescriptor("jackrabbit.cluster.id");
            if(clusterId!=null){
                clusterNodeId = clusterId;
            }
        }
        return clusterNodeId;
    }


}

package org.onehippo.forge.webservices.jaxrs.jcr;

import java.net.URI;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.annotations.GZIP;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.onehippo.forge.webservices.jaxrs.jcr.model.JcrNode;
import org.onehippo.forge.webservices.jaxrs.jcr.model.JcrProperty;
import org.onehippo.forge.webservices.jaxrs.jcr.util.JcrDataBindingHelper;
import org.onehippo.forge.webservices.jaxrs.jcr.util.RepositoryConnectionUtils;
import org.onehippo.forge.webservices.jaxrs.jcr.util.ResponseConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource providing CRUD operations for JCR properties.
 *
 * @author jreijn
 */
@GZIP
@Api(value = "properties", description = "API for working with JCR properties")
@Path("properties")
@CrossOriginResourceSharing(allowAllOrigins = true)
public class PropertiesResource {

    private static Logger log = LoggerFactory.getLogger(NodesResource.class);

    @Context
    private HttpServletRequest request;

    /**
     * Gets a property by its path.
     */
    @GET
    @Path("{path:.*}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @ApiOperation(value = "Get a property", notes = "Returns a property from the specified path")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ResponseConstants.STATUS_MESSAGE_OK, response = JcrNode.class),
            @ApiResponse(code = 401, message = ResponseConstants.STATUS_MESSAGE_UNAUTHORIZED),
            @ApiResponse(code = 404, message = ResponseConstants.STATUS_MESSAGE_NODE_NOT_FOUND),
            @ApiResponse(code = 500, message = ResponseConstants.STATUS_MESSAGE_ERROR_OCCURRED)
    })
    public Response getPropertyByPath(@ApiParam(value = "Path of the node to retrieve e.g '/content/hippostd:foldertype'.", required = true) @PathParam("path") String path) throws RepositoryException {

        final Session session = RepositoryConnectionUtils.createSession(request);
        JcrProperty jcrProperty = null;
        String absolutePath = StringUtils.defaultIfEmpty(path, "/");
        if (!absolutePath.startsWith("/")) {
            absolutePath = "/" + absolutePath;
        }

        try {
            if (!session.propertyExists(absolutePath)) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            final Property property = session.getProperty(absolutePath);
            jcrProperty = JcrDataBindingHelper.getPropertyRepresentation(property);
        } catch (RepositoryException e) {
            log.error("Error: {}", e);
            throw new WebApplicationException(e);
        } finally {
            RepositoryConnectionUtils.cleanupSession(session);
        }
        return Response.ok(jcrProperty).build();
    }

    /**
     * Creates a new property.
     */
    @POST
    @Path("{path:.*}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @ApiOperation(value = "Add a property to a node", notes = "Adds a property to a node")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = ResponseConstants.STATUS_MESSAGE_CREATED),
            @ApiResponse(code = 401, message = ResponseConstants.STATUS_MESSAGE_UNAUTHORIZED),
            @ApiResponse(code = 404, message = ResponseConstants.STATUS_MESSAGE_NODE_NOT_FOUND),
            @ApiResponse(code = 403, message = ResponseConstants.STATUS_MESSAGE_ACCESS_DENIED),
            @ApiResponse(code = 500, message = ResponseConstants.STATUS_MESSAGE_ERROR_OCCURRED)
    })
    public Response createPropertyByPath(@ApiParam(required = true, value = "Path of the node to which to add the property to e.g. '/content/documents/'")
                                         @PathParam("path") @DefaultValue("/") String parentPath,
                                         @Context UriInfo ui,
                                         JcrProperty jcrProperty) throws RepositoryException {

        final Session session = RepositoryConnectionUtils.createSession(request);

        String absolutePath = StringUtils.defaultIfEmpty(parentPath, "/");
        if (!absolutePath.startsWith("/")) {
            absolutePath = "/" + absolutePath;
        }

        if (!session.nodeExists(absolutePath)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (StringUtils.isEmpty(jcrProperty.getName())) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (StringUtils.isEmpty(jcrProperty.getType())) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        URI newPropertyUri = null;
        try {
            final Node parentNode = session.getNode(absolutePath);
            JcrDataBindingHelper.addPropertyToNode(parentNode, jcrProperty);
            session.save();
            UriBuilder ub = ui.getBaseUriBuilder().path(this.getClass()).path(this.getClass(), "getPropertyByPath");
            newPropertyUri = ub.build(parentNode.getProperty(jcrProperty.getName()).getPath().substring(1));
        } catch (Exception e) {
            throw new WebApplicationException(e);
        } finally {
            RepositoryConnectionUtils.cleanupSession(session);
        }
        return Response.created(newPropertyUri).build();
    }

    /**
     * Delete a property by it's path
     *
     * @param path the path to the node
     * @return the Response status
     * @throws RepositoryException
     */
    @DELETE
    @Path("{path:.*}")
    @ApiOperation(value = "Delete a property", notes = "Deletes a property")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = ResponseConstants.STATUS_MESSAGE_DELETED),
            @ApiResponse(code = 404, message = ResponseConstants.STATUS_MESSAGE_NODE_NOT_FOUND),
            @ApiResponse(code = 500, message = ResponseConstants.STATUS_MESSAGE_ERROR_OCCURRED)
    })
    public Response deletePropertyByPath(@ApiParam(required = true, value = "Path of the property to delete e.g. '/content/hippostd:foldertype'.")
                                         @PathParam("path") String path) throws RepositoryException {

        final Session session = RepositoryConnectionUtils.createSession(request);
        String absolutePath = path;

        if (StringUtils.isBlank(path)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!absolutePath.startsWith("/")) {
            absolutePath = "/" + absolutePath;
        }

        if (!session.propertyExists(absolutePath)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        try {
            final Property property = session.getProperty(absolutePath);
            property.remove();
            session.save();
        } catch (Exception e) {
            throw new WebApplicationException(e);
        } finally {
            RepositoryConnectionUtils.cleanupSession(session);
        }
        return Response.noContent().build();

    }

}

package org.onehippo.forge.webservices.v1.jcr;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.annotations.GZIP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GZIP
@Api(value = "v1/properties", description = "API for working with JCR properties")
@Path("v1/properties")
public class PropertyResource {

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
     * Delete a property by it's path
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
    public Response deleteNodeByPath(@ApiParam(required = true, value = "Path of the property to delete e.g. '/content/hippostd:foldertype'.")
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

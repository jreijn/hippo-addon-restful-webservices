package org.onehippo.forge.restservices.v1.jcr;

import java.net.URI;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource providing CRUD operations on JCR nodes.
 *
 * @author jreijn
 */
@GZIP
@Api(value = "v1/nodes", description = "API for working with JCR nodes")
@Path("v1/nodes")
public class NodesResource {

    private static Logger log = LoggerFactory.getLogger(NodesResource.class);

    /**
     * Gets a node by its path.
     */
    @GET
    @Path("{path:.*}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @ApiOperation(value = "Get a node", notes = "Returns a node from the specified path")

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ResponseConstants.STATUS_MESSAGE_OK, response = JcrNode.class),
            @ApiResponse(code = 401, message = ResponseConstants.STATUS_MESSAGE_UNAUTHORIZED),
            @ApiResponse(code = 404, message = ResponseConstants.STATUS_MESSAGE_NODE_NOT_FOUND),
            @ApiResponse(code = 500, message = ResponseConstants.STATUS_MESSAGE_ERROR_OCCURRED)
    })
    public Response getNodeByPath(@ApiParam(value = "Path of the node to retrieve", required = true) @PathParam("path") @DefaultValue("/") String path,
                                  @ApiParam(value = "Depth of the retrieval", required = false) @QueryParam("depth") @DefaultValue("0") int depth) throws RepositoryException {

        final Session session = RepositoryConnectionUtils.createSession("admin", "admin");
        JcrNode jcrNode = null;
        String absolutePath = StringUtils.defaultIfEmpty(path, "/");
        if (!absolutePath.startsWith("/")) {
            absolutePath = "/" + absolutePath;
        }

        try {
            if (!session.nodeExists(absolutePath)) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            final Node node = session.getNode(absolutePath);
            jcrNode = JcrBindingHelper.getNodeRepresentation(node, depth);
        } catch (RepositoryException e) {
            log.error("Error: {}", e);
            throw new WebApplicationException(e);
        } finally {
            RepositoryConnectionUtils.cleanupSession(session);
        }
        return Response.ok(jcrNode).build();
    }

    /**
     * Creates a new node and populates it with the supplied properties.
     */
    @POST
    @Path("{path:.*}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @ApiOperation(value = "Create a node", notes = "Creates a node and adds provided properties")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ResponseConstants.STATUS_MESSAGE_OK),
            @ApiResponse(code = 201, message = ResponseConstants.STATUS_MESSAGE_CREATED),
            @ApiResponse(code = 401, message = ResponseConstants.STATUS_MESSAGE_UNAUTHORIZED),
            @ApiResponse(code = 404, message = ResponseConstants.STATUS_MESSAGE_NODE_NOT_FOUND),
            @ApiResponse(code = 403, message = ResponseConstants.STATUS_MESSAGE_ACCESS_DENIED),
            @ApiResponse(code = 500, message = ResponseConstants.STATUS_MESSAGE_ERROR_OCCURRED)
    })
    public Response createNodeByPath(@ApiParam(required = true, value = "Path of the node to which to add the new node e.g. '/content/documents/'")
                                     @PathParam("path") @DefaultValue("/") String parentPath,
                                     @Context UriInfo ui,
                                     JcrNode jcrNode) throws RepositoryException {

        final Session session = RepositoryConnectionUtils.createSession("admin", "admin");

        String absolutePath = StringUtils.defaultIfEmpty(parentPath, "/");
        if (!absolutePath.startsWith("/")) {
            absolutePath = "/" + absolutePath;
        }

        if (!session.nodeExists(absolutePath)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (StringUtils.isEmpty(jcrNode.getName())) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (StringUtils.isEmpty(jcrNode.getPrimaryType())) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        URI newNodeUri = null;
        try {
            final Node parentNode = session.getNode(absolutePath);
            final Node node = parentNode.addNode(jcrNode.getName(), jcrNode.getPrimaryType());
            JcrBindingHelper.addMixinsFromRepresentation(node, jcrNode.getMixinTypes());
            JcrBindingHelper.addPropertiesFromRepresentation(node, jcrNode.getProperties());
            UriBuilder ub = ui.getAbsolutePathBuilder().path(this.getClass(), "getNodeByPath");
            newNodeUri = ub.build(node.getName());
            session.save();
        } catch (Exception e) {
            throw new WebApplicationException(e);
        } finally {
            RepositoryConnectionUtils.cleanupSession(session);
        }

        return Response.created(newNodeUri).build();
    }

    @DELETE
    @Path("{path:.*}")
    @ApiOperation(value = "Delete a node", notes = "Deletes a node (and child-nodes)")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = ResponseConstants.STATUS_MESSAGE_DELETED),
            @ApiResponse(code = 404, message = ResponseConstants.STATUS_MESSAGE_NODE_NOT_FOUND),
            @ApiResponse(code = 500, message = ResponseConstants.STATUS_MESSAGE_ERROR_OCCURRED)
    })
    public Response deleteNodeByPath(@ApiParam(required = true, value = "Path of the node to delete e.g. '/content/documents/'")
                                     @PathParam("path") String path) throws RepositoryException {

        final Session session = RepositoryConnectionUtils.createSession("admin", "admin");
        String absolutePath = path;

        if(StringUtils.isBlank(path)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!absolutePath.startsWith("/")) {
            absolutePath = "/" + absolutePath;
        }

        if (!session.nodeExists(absolutePath)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        try {
            final Node node = session.getNode(absolutePath);
            node.remove();
            session.save();
        } catch (Exception e) {
            throw new WebApplicationException(e);
        } finally {
            RepositoryConnectionUtils.cleanupSession(session);
        }
        return Response.noContent().build();

    }

}

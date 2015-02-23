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

package org.onehippo.forge.webservices.jaxrs.jcr;

import java.net.URI;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.onehippo.forge.webservices.jaxrs.jcr.model.JcrNode;
import org.onehippo.forge.webservices.jaxrs.jcr.util.JcrDataBindingHelper;
import org.onehippo.forge.webservices.jaxrs.jcr.util.JcrSessionUtil;
import org.onehippo.forge.webservices.jaxrs.jcr.util.ResponseConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource providing CRUD operations on JCR nodes.
 */
@GZIP
@Api(value = "v1/nodes", description = "JCR node API", position = 2)
@Path("v1/nodes")
@CrossOriginResourceSharing(allowAllOrigins = true)
public class NodesResource {

    private static final Logger log = LoggerFactory.getLogger(NodesResource.class);

    @Context
    private HttpServletRequest request;

    /**
     * Gets a node by its path.
     */
    @GET
    @Path("{path:.*}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Get a node", notes = "Returns a node from the specified path", position = 1)

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ResponseConstants.STATUS_MESSAGE_OK, response = JcrNode.class),
            @ApiResponse(code = 401, message = ResponseConstants.STATUS_MESSAGE_UNAUTHORIZED),
            @ApiResponse(code = 404, message = ResponseConstants.STATUS_MESSAGE_NODE_NOT_FOUND),
            @ApiResponse(code = 500, message = ResponseConstants.STATUS_MESSAGE_ERROR_OCCURRED)
    })
    public Response getNodeByPath(@ApiParam(value = "Path of the node to retrieve", required = true) @PathParam("path") @DefaultValue("/") String path,
                                  @ApiParam(value = "Depth of the retrieval", required = false) @QueryParam("depth") @DefaultValue("0") int depth,
                                  @Context UriInfo ui) throws RepositoryException {


        JcrNode jcrNode = null;
        try {
            Session session = JcrSessionUtil.getSessionFromRequest(request);
            String absolutePath = StringUtils.defaultIfEmpty(path, "/");
            if (!absolutePath.startsWith("/")) {
                absolutePath = "/" + absolutePath;
            }

            if (!session.nodeExists(absolutePath)) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            final Node node = session.getNode(absolutePath);
            jcrNode = JcrDataBindingHelper.getNodeRepresentation(node, depth);


        } catch (RepositoryException e) {
            log.error("Error: {}", e);
            throw new WebApplicationException(e);
        }
        return Response.ok(jcrNode).build();
    }

    /**
     * Adds new node and populates it with the supplied properties.
     */
    @POST
    @Path("{path:.*}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Creates a new sub node", notes = "Adds a node and it's properties as a child of the provided location", position = 2)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ResponseConstants.STATUS_MESSAGE_OK),
            @ApiResponse(code = 201, message = ResponseConstants.STATUS_MESSAGE_CREATED),
            @ApiResponse(code = 400, message = ResponseConstants.STATUS_MESSAGE_BAD_REQUEST),
            @ApiResponse(code = 401, message = ResponseConstants.STATUS_MESSAGE_UNAUTHORIZED),
            @ApiResponse(code = 404, message = ResponseConstants.STATUS_MESSAGE_NODE_NOT_FOUND),
            @ApiResponse(code = 403, message = ResponseConstants.STATUS_MESSAGE_ACCESS_DENIED),
            @ApiResponse(code = 500, message = ResponseConstants.STATUS_MESSAGE_ERROR_OCCURRED)
    })
    public Response createNodeByPath(@ApiParam(required = true, value = "Path of the node to which to add the new node e.g. '/content/documents/'")
                                     @PathParam("path") @DefaultValue("/") String parentPath,
                                     @Context UriInfo ui,
                                     JcrNode jcrNode) throws RepositoryException {

        URI newNodeUri = null;

        try {
            Session session = JcrSessionUtil.getSessionFromRequest(request);
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

            final Node parentNode = session.getNode(absolutePath);
            final Node node = parentNode.addNode(jcrNode.getName(), jcrNode.getPrimaryType());
            JcrDataBindingHelper.addMixinsFromRepresentation(node, jcrNode.getMixinTypes());
            JcrDataBindingHelper.addPropertiesFromRepresentation(node, jcrNode.getProperties());
            JcrDataBindingHelper.addChildNodesFromRepresentation(node, jcrNode.getNodes());
            UriBuilder ub = ui.getAbsolutePathBuilder().path(this.getClass(), "getNodeByPath");
            newNodeUri = ub.build(node.getName());
            session.save();
        } catch (Exception e) {
            log.error("Error: {}", e);
            throw new WebApplicationException(e);
        }
        return Response.created(newNodeUri).build();
    }

    /**
     * Updates a node. To update a node you need to provide the entire entity. The entity will be replaced with the
     * provided data. Be careful in case you want to update entire node structures, because the UUIDs will be
     * regenerated
     */
    @PUT
    @Path("{path:.*}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Update a node", notes = "To update a node you need to provide the entire entity. The entity will be replaced with the " +
            " provided data. Be careful in case you want to update entire node structures, because the UUIDs will be " +
            " regenerated", position = 3)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ResponseConstants.STATUS_MESSAGE_OK),
            @ApiResponse(code = 204, message = ResponseConstants.STATUS_MESSAGE_UPDATED),
            @ApiResponse(code = 400, message = ResponseConstants.STATUS_MESSAGE_BAD_REQUEST),
            @ApiResponse(code = 401, message = ResponseConstants.STATUS_MESSAGE_UNAUTHORIZED),
            @ApiResponse(code = 404, message = ResponseConstants.STATUS_MESSAGE_NODE_NOT_FOUND),
            @ApiResponse(code = 403, message = ResponseConstants.STATUS_MESSAGE_ACCESS_DENIED),
            @ApiResponse(code = 500, message = ResponseConstants.STATUS_MESSAGE_ERROR_OCCURRED)
    })
    public Response updateNodeByPath(@ApiParam(required = true, value = "Path of the node to update. '/content/documents/'")
                                     @PathParam("path") String parentPath,
                                     @Context UriInfo ui,
                                     JcrNode jcrNode) throws RepositoryException {
        try {
            Session session = JcrSessionUtil.getSessionFromRequest(request);
            String absolutePath = StringUtils.defaultIfEmpty(parentPath, "/");
            if (!absolutePath.startsWith("/")) {
                absolutePath = "/" + absolutePath;
            }

            if (StringUtils.isEmpty(jcrNode.getName())) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            if (StringUtils.isEmpty(jcrNode.getPrimaryType())) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            if (!session.nodeExists(absolutePath)) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            final Node nodeToUpdate = session.getNode(absolutePath);
            final Node parentNode = nodeToUpdate.getParent();
            nodeToUpdate.remove();

            final Node node = parentNode.addNode(jcrNode.getName(), jcrNode.getPrimaryType());
            JcrDataBindingHelper.addMixinsFromRepresentation(node, jcrNode.getMixinTypes());
            JcrDataBindingHelper.addPropertiesFromRepresentation(node, jcrNode.getProperties());
            JcrDataBindingHelper.addChildNodesFromRepresentation(node, jcrNode.getNodes());
            session.save();
        } catch (Exception e) {
            log.error("Error: {}", e);
            throw new WebApplicationException(e);
        }

        return Response.noContent().build();
    }

    @DELETE
    @Path("{path:.*}")
    @ApiOperation(value = "Delete a node", notes = "Deletes a node (and child-nodes)", position = 4)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = ResponseConstants.STATUS_MESSAGE_DELETED),
            @ApiResponse(code = 404, message = ResponseConstants.STATUS_MESSAGE_NODE_NOT_FOUND),
            @ApiResponse(code = 500, message = ResponseConstants.STATUS_MESSAGE_ERROR_OCCURRED)
    })
    public Response deleteNodeByPath(@ApiParam(required = true, value = "Path of the node to delete e.g. '/content/documents/'")
                                     @PathParam("path") String path) throws RepositoryException {

        try {
            String absolutePath = path;
            Session session = JcrSessionUtil.getSessionFromRequest(request);
            if (StringUtils.isBlank(path)) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            if (!absolutePath.startsWith("/")) {
                absolutePath = "/" + absolutePath;
            }

            if (!session.nodeExists(absolutePath)) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            final Node node = session.getNode(absolutePath);
            node.remove();
            session.save();
        } catch (Exception e) {
            log.error("Error: {}", e);
            throw new WebApplicationException(e);
        }
        return Response.noContent().build();

    }

}

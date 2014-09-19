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
import javax.jcr.Property;
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
 */
@GZIP
@Api(value = "properties", description = "JCR property API", position = 3)
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

        Session session = null;
        JcrProperty jcrProperty = null;

        try {
            session = RepositoryConnectionUtils.createSession(request);
            String absolutePath = StringUtils.defaultIfEmpty(path, "/");
            if (!absolutePath.startsWith("/")) {
                absolutePath = "/" + absolutePath;
            }

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
            @ApiResponse(code = 201, message = ResponseConstants.STATUS_MESSAGE_CREATED, response = JcrProperty.class),
            @ApiResponse(code = 401, message = ResponseConstants.STATUS_MESSAGE_UNAUTHORIZED),
            @ApiResponse(code = 404, message = ResponseConstants.STATUS_MESSAGE_NODE_NOT_FOUND),
            @ApiResponse(code = 403, message = ResponseConstants.STATUS_MESSAGE_ACCESS_DENIED),
            @ApiResponse(code = 500, message = ResponseConstants.STATUS_MESSAGE_ERROR_OCCURRED)
    })
    public Response createPropertyByPath(@ApiParam(required = true, value = "Path of the node to which to add the property to e.g. '/content/documents/'")
                                         @PathParam("path") @DefaultValue("/") String parentPath,
                                         @Context UriInfo ui,
                                         JcrProperty jcrProperty) throws RepositoryException {

        Session session = null;
        URI newPropertyUri = null;
        try {
            session = RepositoryConnectionUtils.createSession(request);
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
     * Updates a property.
     */
    @PUT
    @Path("{path:.*}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @ApiOperation(value = "Updates a property of a node", notes = "Updates a property of a node")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = ResponseConstants.STATUS_MESSAGE_UPDATED),
            @ApiResponse(code = 401, message = ResponseConstants.STATUS_MESSAGE_UNAUTHORIZED),
            @ApiResponse(code = 404, message = ResponseConstants.STATUS_MESSAGE_PROPERTY_NOT_FOUND),
            @ApiResponse(code = 403, message = ResponseConstants.STATUS_MESSAGE_ACCESS_DENIED),
            @ApiResponse(code = 500, message = ResponseConstants.STATUS_MESSAGE_ERROR_OCCURRED)
    })
    public Response updatePropertyByPath(@ApiParam(required = true, value = "Path of the property to update e.g. '/content/documents/hippostd:foldertypes'")
                                         @PathParam("path") @DefaultValue("/") String parentPath,
                                         @Context UriInfo ui,
                                         JcrProperty jcrProperty) throws RepositoryException {
        Session session = null;
        try {
            session = RepositoryConnectionUtils.createSession(request);

            String absolutePath = StringUtils.defaultIfEmpty(parentPath, "/");
            if (!absolutePath.startsWith("/")) {
                absolutePath = "/" + absolutePath;
            }

            if (!session.propertyExists(absolutePath)) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            if (StringUtils.isEmpty(jcrProperty.getName())) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            if (StringUtils.isEmpty(jcrProperty.getType())) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            final Property property = session.getProperty(absolutePath);
            final Node node = property.getParent();
            JcrDataBindingHelper.addPropertyToNode(node,jcrProperty);
            session.save();
        } catch (Exception e) {
            throw new WebApplicationException(e);
        } finally {
            RepositoryConnectionUtils.cleanupSession(session);
        }
        return Response.noContent().build();
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
        Session session = null;
        try {
            session = RepositoryConnectionUtils.createSession(request);
            String absolutePath = path;

            if (!absolutePath.startsWith("/")) {
                absolutePath = "/" + absolutePath;
            }

            if (StringUtils.isBlank(path)) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            if (!session.propertyExists(absolutePath)) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

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

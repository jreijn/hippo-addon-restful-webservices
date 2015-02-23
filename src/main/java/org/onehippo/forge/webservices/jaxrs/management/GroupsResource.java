/*
 * Copyright 2015 Hippo B.V. (http://www.onehippo.com)
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

package org.onehippo.forge.webservices.jaxrs.management;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.servlet.http.HttpServletRequest;
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

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import org.apache.jackrabbit.util.ISO9075;
import org.apache.jackrabbit.util.Text;
import org.hippoecm.repository.api.HippoNodeType;
import org.hippoecm.repository.api.NodeNameCodec;
import org.onehippo.forge.webservices.jaxrs.exception.ResponseExceptionRepresentation;
import org.onehippo.forge.webservices.jaxrs.hateoas.Link;
import org.onehippo.forge.webservices.jaxrs.jcr.util.JcrSessionUtil;
import org.onehippo.forge.webservices.jaxrs.jcr.util.ResponseConstants;
import org.onehippo.forge.webservices.jaxrs.management.model.Group;
import org.onehippo.forge.webservices.jaxrs.management.model.GroupCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("v1/groups")
public class GroupsResource {

    private static final Logger log = LoggerFactory.getLogger(GroupsResource.class);

    private final static String PROP_DESCRIPTION = "hipposys:description";
    private final static String PROP_MEMBERS = "hipposys:members";
    private final static String QUERY_ALL = "select * from hipposys:group";
    private final static String QUERY_GROUP = "SELECT * FROM hipposys:group WHERE fn:name()='{}'";

    @Context
    private HttpServletRequest request;


    @Path("/")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Get all groups", notes = "Returns a list of groups", position = 1)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ResponseConstants.STATUS_MESSAGE_OK, response = GroupCollection.class),
            @ApiResponse(code = 401, message = ResponseConstants.STATUS_MESSAGE_UNAUTHORIZED),
            @ApiResponse(code = 404, message = ResponseConstants.STATUS_MESSAGE_NODE_NOT_FOUND),
            @ApiResponse(code = 500, message = ResponseConstants.STATUS_MESSAGE_ERROR_OCCURRED)
    })
    public Response getGroups(@Context UriInfo ui,
                              @QueryParam("limit") @DefaultValue("20") long limit,
                              @QueryParam("offset") @DefaultValue("0") long offset) throws RepositoryException {
        GroupCollection groups;
        try {
            List<Group> groupList = new ArrayList<Group>();
            final Query query = getQueryManager().createQuery(QUERY_ALL, Query.SQL);
            query.setLimit(limit);
            query.setOffset(offset);
            final QueryResult result = query.execute();
            final NodeIterator nodes = result.getNodes();
            while (nodes.hasNext()) {
                final Node node = nodes.nextNode();
                final Group groupFromNode = getGroupFromNode(node);
                UriBuilder ub = ui.getAbsolutePathBuilder().path(this.getClass(), "getGroupByName");
                groupFromNode.setUri(ub.build(groupFromNode.getName()));
                groupList.add(groupFromNode);
            }
            groups = new GroupCollection(groupList);

            UriBuilder ub = ui.getAbsolutePathBuilder().path(this.getClass(), "getGroups");
            ub.queryParam("offset", offset + limit);
            if (limit > 0) {
                ub.queryParam("limit", limit);
            }
            final URI nextUri = ub.build();

            ub.replaceQueryParam("offset", 0);
            ub.replaceQueryParam("limit", 20);
            final URI firstUri = ub.build();
            //TODO: Take care of negative offset
            if (offset > 0) {
                ub.replaceQueryParam("offset", offset - limit);
                ub.replaceQueryParam("limit", limit);
                final URI prevUri = ub.build();
                groups.addLink(new Link("prev", prevUri));
            }
            groups.addLink(new Link("first", firstUri));
            groups.addLink(new Link("next", nextUri));

        } catch (RepositoryException e) {
            log.error("Error: {}", e);
            throw new WebApplicationException(e);
        }
        return Response.ok(groups).build();
    }

    @GET
    @Path("/{groupName}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Get a group by it's name", notes = "Returns a group by it's name", position = 1)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ResponseConstants.STATUS_MESSAGE_OK, response = Group.class),
            @ApiResponse(code = 401, message = ResponseConstants.STATUS_MESSAGE_UNAUTHORIZED),
            @ApiResponse(code = 404, message = ResponseConstants.STATUS_MESSAGE_NODE_NOT_FOUND),
            @ApiResponse(code = 500, message = ResponseConstants.STATUS_MESSAGE_ERROR_OCCURRED)
    })
    public Response getGroupByName(@ApiParam(value = "Name of the group to retrieve", required = true) @PathParam("groupName") String groupName,
                                   @Context UriInfo ui) throws RepositoryException {
        Group group;
        final Node groupNodeByName = getGroupNodeByName(groupName);
        if (groupNodeByName == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        group = getGroupFromNode(groupNodeByName);
        return Response.ok(group).build();
    }

    /**
     * Adds a new group and populates it with the supplied properties.
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Creates a new group", notes = "Adds a new group", position = 2)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ResponseConstants.STATUS_MESSAGE_OK),
            @ApiResponse(code = 201, message = ResponseConstants.STATUS_MESSAGE_CREATED),
            @ApiResponse(code = 400, message = ResponseConstants.STATUS_MESSAGE_BAD_REQUEST),
            @ApiResponse(code = 401, message = ResponseConstants.STATUS_MESSAGE_UNAUTHORIZED),
            @ApiResponse(code = 404, message = ResponseConstants.STATUS_MESSAGE_NODE_NOT_FOUND),
            @ApiResponse(code = 403, message = ResponseConstants.STATUS_MESSAGE_ACCESS_DENIED),
            @ApiResponse(code = 500, message = ResponseConstants.STATUS_MESSAGE_ERROR_OCCURRED)
    })
    public Response createGroup(@ApiParam(required = true, value = "Name of the new group") Group group,
                                @Context UriInfo ui) throws RepositoryException {
        URI newUserUri;
        try {
            Session session = JcrSessionUtil.getSessionFromRequest(request);
            if (exists(group.getName())) {
                final ResponseExceptionRepresentation responseExceptionRepresentation = new ResponseExceptionRepresentation(
                        Response.Status.CONFLICT.getStatusCode(), "Group with name '" + group.getName() + "' already exists.");
                return Response.status(Response.Status.CONFLICT).entity(responseExceptionRepresentation).build();
            } else if (group.isExternal()) {
                final ResponseExceptionRepresentation responseExceptionRepresentation = new ResponseExceptionRepresentation(
                        Response.Status.CONFLICT.getStatusCode(), "External managed group can't be created through this interface.");
                return Response.status(Response.Status.CONFLICT).entity(responseExceptionRepresentation).build();
            }
            final Node node = create(group);
            UriBuilder ub = ui.getAbsolutePathBuilder().path(this.getClass(), "getGroupByName");
            newUserUri = ub.build(node.getName());
            session.save();
        } catch (RepositoryException e) {
            log.error("Error: {}", e);
            throw new WebApplicationException(e);
        }
        return Response.created(newUserUri).build();
    }

    @DELETE
    @Path("/{groupName}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Deletes a group by it's name", notes = "Deletes a group by name", position = 1)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = ResponseConstants.STATUS_MESSAGE_DELETED),
            @ApiResponse(code = 401, message = ResponseConstants.STATUS_MESSAGE_UNAUTHORIZED),
            @ApiResponse(code = 404, message = ResponseConstants.STATUS_MESSAGE_NODE_NOT_FOUND),
            @ApiResponse(code = 500, message = ResponseConstants.STATUS_MESSAGE_ERROR_OCCURRED)
    })
    public Response deleteGroupByName(@ApiParam(value = "Name of the group to delete", required = true) @PathParam("groupName") String groupName,
                                     @Context UriInfo ui) throws RepositoryException {
        try {
            final Session session = JcrSessionUtil.getSessionFromRequest(request);
            Node groupNode = getGroupNodeByName(groupName);
            if (groupNode == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            groupNode.remove();
            session.save();
        } catch (RepositoryException e) {
            log.error("Error: {}", e);
            throw new WebApplicationException(e);
        }
        return Response.noContent().build();
    }

    public boolean exists(String groupname) {
        return getGroupNodeByName(groupname) != null;
    }

    /**
     * Gets the Node for the specified name. If no node with the specified name exists, null is returned.
     *
     * @param groupName the name of the Group to return
     * @return the Node with name groupName
     */
    public Node getGroupNodeByName(final String groupName) {
        final String escapedGroupName = Text.escapeIllegalJcr10Chars(ISO9075.encode(NodeNameCodec.encode(groupName, true)));
        final String queryString = QUERY_GROUP.replace("{}", escapedGroupName);
        try {
            @SuppressWarnings("deprecation") final Query query = getQueryManager().createQuery(queryString, Query.SQL);
            final QueryResult queryResult = query.execute();
            final NodeIterator iterator = queryResult.getNodes();
            if (!iterator.hasNext()) {
                return null;
            }

            final Node node = iterator.nextNode();
            return node;
        } catch (RepositoryException e) {
            log.error("Unable to check if group '{}' exists, returning true", groupName, e);
            return null;
        }
    }

    private Group getGroupFromNode(final Node node) throws RepositoryException {
        final Group group = new Group();
        group.setName(NodeNameCodec.decode(node.getName()));
        if (node.hasProperty(PROP_DESCRIPTION)) {
            group.setDescription(node.getProperty(PROP_DESCRIPTION).getString());
        }
        if (node.isNodeType(HippoNodeType.NT_EXTERNALGROUP)) {
            group.setExternal(true);
        }
        if (node.hasProperty(PROP_MEMBERS)) {
            final Value[] values = node.getProperty(PROP_MEMBERS).getValues();
            List<String> members = new ArrayList<String>();
            for(Value value : values){
                members.add(value.getString());
            }
            group.setMembers(members);
        }
        return group;
    }

    /**
     * Create a new group
     *
     * @throws RepositoryException
     */
    public Node create(Group group) throws RepositoryException {
        if (exists(group.getName())) {
            throw new RepositoryException("Group already exists");
        }

        // FIXME: should be delegated to a groupmanager
        StringBuilder relPath = new StringBuilder();
        relPath.append(HippoNodeType.CONFIGURATION_PATH);
        relPath.append("/");
        relPath.append(HippoNodeType.GROUPS_PATH);
        relPath.append("/");
        relPath.append(NodeNameCodec.encode(group.getName(), true));

        final Node node = JcrSessionUtil.getSessionFromRequest(request).getRootNode().addNode(relPath.toString(), HippoNodeType.NT_GROUP);
        setOrRemoveStringProperty(node, PROP_DESCRIPTION, group.getDescription());
        final List<String> members = group.getMembers();
        if(members!=null){
            setOrRemoveMultiStringProperty(node, PROP_MEMBERS, members.toArray(new String[0]));
        }
        // save parent when adding a node
        node.getParent().getSession().save();
        return node;
    }

    /**
     * Wrapper needed for spi layer which doesn't know if a property exists or not
     *
     * @param node the node on which to put the property
     * @param name the name of the property
     * @param value the value of the property
     * @throws RepositoryException
     */
    private void setOrRemoveStringProperty(Node node, String name, String value) throws RepositoryException {
        if (value == null && !node.hasProperty(name)) {
            return;
        }
        node.setProperty(name, value);
    }

    /**
     * Wrapper needed for spi layer which doesn't know if a property exists or not
     *
     * @param node the node on which to put the property
     * @param name the name of the property
     * @param values the value of the property
     * @throws RepositoryException
     */
    private void setOrRemoveMultiStringProperty(Node node, String name, String[] values) throws RepositoryException {
        if (values == null && !node.hasProperty(name)) {
            return;
        }
        node.setProperty(name, values);
    }


    public QueryManager getQueryManager() throws RepositoryException {
        return JcrSessionUtil.getSessionFromRequest(request).getWorkspace().getQueryManager();
    }

}

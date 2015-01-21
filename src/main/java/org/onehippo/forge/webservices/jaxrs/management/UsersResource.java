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

import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
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

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.apache.jackrabbit.util.ISO9075;
import org.apache.jackrabbit.util.Text;
import org.hippoecm.repository.PasswordHelper;
import org.hippoecm.repository.api.HippoNodeType;
import org.hippoecm.repository.api.NodeNameCodec;
import org.onehippo.forge.webservices.jaxrs.management.model.Group;
import org.onehippo.forge.webservices.jaxrs.management.model.User;
import org.onehippo.forge.webservices.jaxrs.management.model.UserCollection;
import org.onehippo.forge.webservices.jaxrs.exception.ResponseExceptionRepresentation;
import org.onehippo.forge.webservices.jaxrs.hateoas.Link;
import org.onehippo.forge.webservices.jaxrs.jcr.util.JcrSessionUtil;
import org.onehippo.forge.webservices.jaxrs.jcr.util.ResponseConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(value = "users", description = "Users API", position = 6)
@Path("/users")
@CrossOriginResourceSharing(allowAllOrigins = true)
public class UsersResource {

    private static final Logger log = LoggerFactory.getLogger(UsersResource.class);

    private static final String NT_USER = "hipposys:user";

    public static final String PROP_FIRSTNAME = "hipposys:firstname";
    public static final String PROP_LASTNAME = "hipposys:lastname";
    public static final String PROP_EMAIL = "hipposys:email";
    public static final String PROP_SYSTEM = HippoNodeType.HIPPO_SYSTEM;

    public static final String PROP_PASSWORD = HippoNodeType.HIPPO_PASSWORD;
    public static final String PROP_PASSKEY = HippoNodeType.HIPPO_PASSKEY;
    public static final String PROP_PROVIDER = HippoNodeType.HIPPO_SECURITYPROVIDER;
    public static final String PROP_PREVIOUSPASSWORDS = HippoNodeType.HIPPO_PREVIOUSPASSWORDS;
    public static final String PROP_PASSWORDLASTMODIFIED = HippoNodeType.HIPPO_PASSWORDLASTMODIFIED;

    private final static String PROP_DESCRIPTION = "hipposys:description";

    private static final String QUERY_USER = "SELECT * FROM hipposys:user WHERE fn:name()='{}'";
    private static final String QUERY_USERS = "SELECT * FROM hipposys:user";

    private static final String QUERY_LOCAL_MEMBERSHIPS = "//element(*, hipposys:group)[@hipposys:members='{}']";
    //TODO
    private static final String QUERY_EXTERNAL_MEMBERSHIPS = "//element(*, hipposys:externalgroup)[@hipposys:members='{}']";

    @Context
    private HttpServletRequest request;

    @Path("/")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Get all users", notes = "Returns a list of users", position = 1)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ResponseConstants.STATUS_MESSAGE_OK, response = User.class),
            @ApiResponse(code = 401, message = ResponseConstants.STATUS_MESSAGE_UNAUTHORIZED),
            @ApiResponse(code = 404, message = ResponseConstants.STATUS_MESSAGE_NODE_NOT_FOUND),
            @ApiResponse(code = 500, message = ResponseConstants.STATUS_MESSAGE_ERROR_OCCURRED)
    })
    public Response getUsers(@Context UriInfo ui,
                             @QueryParam("limit") @DefaultValue("20") long limit,
                             @QueryParam("offset") @DefaultValue("0") long offset) throws RepositoryException {
        UserCollection users;
        try {
            List<User> userList = new ArrayList<User>();
            final Query query = getQueryManager().createQuery(QUERY_USERS, Query.SQL);
            query.setLimit(limit);
            query.setOffset(offset);
            final QueryResult result = query.execute();
            final NodeIterator nodes = result.getNodes();
            while (nodes.hasNext()) {
                final Node node = nodes.nextNode();
                final User userFromNode = createUserFromNode(node);
                UriBuilder ub = ui.getAbsolutePathBuilder().path(this.getClass(), "getUserByName");
                userFromNode.setUri(ub.build(userFromNode.getUsername()));
                userList.add(userFromNode);
            }
            users = new UserCollection(userList);

            UriBuilder ub = ui.getAbsolutePathBuilder().path(this.getClass(),"getUsers");
            ub.queryParam("offset", offset + limit);
            if(limit>0) {
                ub.queryParam("limit", limit);
            }
            final URI nextUri = ub.build();

            ub.replaceQueryParam("offset",0);
            ub.replaceQueryParam("limit",20);
            final URI firstUri = ub.build();
            //TODO: Take care of negative offset
            if(offset>0) {
                ub.replaceQueryParam("offset", offset - limit);
                ub.replaceQueryParam("limit", limit);
                final URI prevUri = ub.build();
                users.addLink(new Link("prev", prevUri));
            }
            users.addLink(new Link("first", firstUri));
            users.addLink(new Link("next", nextUri));

        } catch (RepositoryException e) {
            log.error("Error: {}", e);
            throw new WebApplicationException(e);
        }
        return Response.ok(users).build();
    }

    /**
     * Adds a new user and populates it with the supplied properties.
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Creates a new user", notes = "Adds a user", position = 2)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ResponseConstants.STATUS_MESSAGE_OK),
            @ApiResponse(code = 201, message = ResponseConstants.STATUS_MESSAGE_CREATED),
            @ApiResponse(code = 400, message = ResponseConstants.STATUS_MESSAGE_BAD_REQUEST),
            @ApiResponse(code = 401, message = ResponseConstants.STATUS_MESSAGE_UNAUTHORIZED),
            @ApiResponse(code = 404, message = ResponseConstants.STATUS_MESSAGE_NODE_NOT_FOUND),
            @ApiResponse(code = 403, message = ResponseConstants.STATUS_MESSAGE_ACCESS_DENIED),
            @ApiResponse(code = 500, message = ResponseConstants.STATUS_MESSAGE_ERROR_OCCURRED)
    })
    public Response createUser(@ApiParam(required = true, value = "Username of the new user") User user,
                               @Context UriInfo ui) throws RepositoryException {
        URI newUserUri;
        try {
            Session session = JcrSessionUtil.getSessionFromRequest(request);
            if (userExists(user.getUsername())) {
                final ResponseExceptionRepresentation responseExceptionRepresentation = new ResponseExceptionRepresentation(
                        Response.Status.CONFLICT.getStatusCode(), "User with name '" + user.getUsername() + "' already exists.");
                return Response.status(Response.Status.CONFLICT).entity(responseExceptionRepresentation).build();
            } else if (user.isExternal()) {
                final ResponseExceptionRepresentation responseExceptionRepresentation = new ResponseExceptionRepresentation(
                        Response.Status.CONFLICT.getStatusCode(), "External managed users can't be created through this interface.");
                return Response.status(Response.Status.CONFLICT).entity(responseExceptionRepresentation).build();
            }
            final Node node = create(user);
            UriBuilder ub = ui.getAbsolutePathBuilder().path(this.getClass(), "getUserByName");
            newUserUri = ub.build(node.getName());
            session.save();
        } catch (RepositoryException e) {
            log.error("Error: {}", e);
            throw new WebApplicationException(e);
        }

        return Response.created(newUserUri).build();
    }

    @GET
    @Path("/{username}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Get a user by username", notes = "Returns a user by username", position = 1)

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ResponseConstants.STATUS_MESSAGE_OK, response = User.class),
            @ApiResponse(code = 401, message = ResponseConstants.STATUS_MESSAGE_UNAUTHORIZED),
            @ApiResponse(code = 404, message = ResponseConstants.STATUS_MESSAGE_NODE_NOT_FOUND),
            @ApiResponse(code = 500, message = ResponseConstants.STATUS_MESSAGE_ERROR_OCCURRED)
    })
    public Response getUserByName(@ApiParam(value = "Name of the user to retrieve", required = true) @PathParam("username") String username,
                                  @Context UriInfo ui) throws RepositoryException {
        User user;
        try {
            Node userNode = getUserNodeByName(username);
            if (userNode == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            user = createUserFromNode(userNode);
            user.setGroups(getLocalMembershipsAsListOfGroups(user.getUsername()));
        } catch (RepositoryException e) {
            log.error("Error: {}", e);
            throw new WebApplicationException(e);
        }
        return Response.ok(user).build();
    }

    @DELETE
    @Path("/{username}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Deletes a user by username", notes = "Deletes a user by username", position = 1)

    @ApiResponses(value = {
            @ApiResponse(code = 204, message = ResponseConstants.STATUS_MESSAGE_DELETED),
            @ApiResponse(code = 401, message = ResponseConstants.STATUS_MESSAGE_UNAUTHORIZED),
            @ApiResponse(code = 404, message = ResponseConstants.STATUS_MESSAGE_NODE_NOT_FOUND),
            @ApiResponse(code = 500, message = ResponseConstants.STATUS_MESSAGE_ERROR_OCCURRED)
    })
    public Response deleteUserByName(@ApiParam(value = "Username of the user to delete", required = true) @PathParam("username") String username,
                                     @Context UriInfo ui) throws RepositoryException {
        try {
            final Session session = JcrSessionUtil.getSessionFromRequest(request);
            Node userNode = getUserNodeByName(username);
            if (userNode == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            userNode.remove();
            session.save();
        } catch (RepositoryException e) {
            log.error("Error: {}", e);
            throw new WebApplicationException(e);
        }
        return Response.noContent().build();
    }

    @GET
    @Path("/me")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Get the current user", notes = "Returns the current user", position = 5)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ResponseConstants.STATUS_MESSAGE_OK, response = User.class),
            @ApiResponse(code = 401, message = ResponseConstants.STATUS_MESSAGE_UNAUTHORIZED),
            @ApiResponse(code = 404, message = ResponseConstants.STATUS_MESSAGE_NODE_NOT_FOUND),
            @ApiResponse(code = 500, message = ResponseConstants.STATUS_MESSAGE_ERROR_OCCURRED)
    })
    public Response getCurrentUser(@Context UriInfo ui) throws RepositoryException {
        User user;
        try {
            final Session session = JcrSessionUtil.getSessionFromRequest(request);
            Node userNode = getUserNodeByName(session.getUserID());
            if (userNode == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            user = createUserFromNode(userNode);
        } catch (RepositoryException e) {
            log.error("Error: {}", e);
            throw new WebApplicationException(e);
        }
        return Response.ok(user).build();
    }

    @GET
    @Path("/{username}/groups")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Get the groups of a user", notes = "Returns list of groups to which a user belongs", position = 5)

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ResponseConstants.STATUS_MESSAGE_OK),
            @ApiResponse(code = 401, message = ResponseConstants.STATUS_MESSAGE_UNAUTHORIZED),
            @ApiResponse(code = 404, message = ResponseConstants.STATUS_MESSAGE_NODE_NOT_FOUND),
            @ApiResponse(code = 500, message = ResponseConstants.STATUS_MESSAGE_ERROR_OCCURRED)
    })
    public Response getGroupsByUserName(
            @ApiParam(value = "Name of the user for which to retrieve the groups", required = true)
            @PathParam("username") String username,
            @Context UriInfo ui) throws RepositoryException {
        List<Group> groups = new ArrayList<Group>();
        if(userExists(username)) {
         groups = getLocalMemberships(username);
        }
        return Response.ok(groups).build();
    }

    private Node getUserNodeByName(final String username) throws RepositoryException {
        Node userNode = null;
        final String queryString = QUERY_USER.replace("{}", Text.escapeIllegalJcr10Chars(ISO9075.encode(NodeNameCodec.encode(username))));
        final Query query = getQueryManager().createQuery(queryString, Query.SQL);
        final NodeIterator nodes = query.execute().getNodes();
        if (nodes.hasNext()) {
            userNode = nodes.nextNode();
        }
        return userNode;
    }

    /**
     * Returns the QueryManager.
     *
     * @return the QueryManager
     * @throws RepositoryException
     */
    private QueryManager getQueryManager() throws RepositoryException {
        final Session session = JcrSessionUtil.getSessionFromRequest(request);
        return session.getWorkspace().getQueryManager();
    }

    private User createUserFromNode(final Node userNode) throws RepositoryException {
        User user = new User(NodeNameCodec.decode(userNode.getName()));
        user.setPath(userNode.getPath().substring(1));
        if (userNode.isNodeType(HippoNodeType.NT_EXTERNALUSER)) {
            user.setExternal(true);
        }
        PropertyIterator pi = userNode.getProperties();

        while (pi.hasNext()) {
            final Property p = pi.nextProperty();
            String name = p.getName();
            if (name.startsWith("jcr:")) {
                //skip
                continue;
            } else if (name.equals(PROP_EMAIL) || name.equalsIgnoreCase("email")) {
                user.setEmail(p.getString());
            } else if (name.equals(PROP_FIRSTNAME) || name.equalsIgnoreCase("firstname")) {
                user.setFirstName(p.getString());
            } else if (name.equals(PROP_LASTNAME) || name.equalsIgnoreCase("lastname")) {
                user.setLastName(p.getString());
            } else if (name.equals(HippoNodeType.HIPPO_ACTIVE)) {
                user.setActive(p.getBoolean());
            } else if (name.equals(PROP_PASSWORD) || name.equals(PROP_PASSKEY) || name.equals(PROP_PREVIOUSPASSWORDS)) {
                // do not expose password hash
                continue;
            } else if (name.equals(PROP_PROVIDER)) {
                continue;
            } else if (name.equals(PROP_PASSWORDLASTMODIFIED)) {
                user.setPasswordLastModified(p.getDate());
            } else if (name.equals(PROP_SYSTEM)) {
                user.setSystem(p.getBoolean());
            }
        }
        return user;
    }

    /**
     * Create a new user with setting security provider by the specified name
     *
     * @throws RepositoryException
     */
    public Node create(final User user) throws RepositoryException {
        StringBuilder relPath = new StringBuilder();
        relPath.append(HippoNodeType.CONFIGURATION_PATH);
        relPath.append("/");
        relPath.append(HippoNodeType.USERS_PATH);
        relPath.append("/");
        relPath.append(NodeNameCodec.encode(user.getUsername(), true));

        Node node = JcrSessionUtil.getSessionFromRequest(request).getRootNode().addNode(relPath.toString(), NT_USER);
        setOrRemoveStringProperty(node, PROP_EMAIL, user.getEmail());
        setOrRemoveStringProperty(node, PROP_FIRSTNAME, user.getFirstName());
        setOrRemoveStringProperty(node, PROP_LASTNAME, user.getLastName());

        Calendar now = Calendar.getInstance();
        node.setProperty(HippoNodeType.HIPPO_PASSWORDLASTMODIFIED, now);
        node.setProperty(HippoNodeType.HIPPO_PASSWORD, createPasswordHash(user.getPassword()));

        return node;
    }

    /**
     * Generate password hash from string.
     *
     * @param password the password
     * @return the hash
     * @throws RepositoryException, the wrapper encoding errors
     */
    public static String createPasswordHash(final String password) throws RepositoryException {
        try {
            return PasswordHelper.getHash(password.toCharArray());
        } catch (NoSuchAlgorithmException e) {
            throw new RepositoryException("Unable to hash password", e);
        } catch (IOException e) {
            throw new RepositoryException("Unable to hash password", e);
        }
    }

    /**
     * Wrapper needed for spi layer which doesn't know if a property exists or not.
     *
     * @param node
     * @param name
     * @param value
     * @throws RepositoryException
     */
    private void setOrRemoveStringProperty(final Node node, final String name, final String value) throws RepositoryException {
        if (value == null && !node.hasProperty(name)) {
            return;
        }
        node.setProperty(name, value);
    }

    /**
     * Checks if the user with username exists.
     *
     * @param username the name of the user to check
     * @return true if the user exists, false otherwise
     */
    public boolean userExists(final String username) {
        final String queryString = QUERY_USER.replace("{}", Text.escapeIllegalJcr10Chars(ISO9075.encode(NodeNameCodec.encode(username))));
        try {
            final Query query = getQueryManager().createQuery(queryString, Query.SQL);
            return query.execute().getNodes().hasNext();
        } catch (RepositoryException e) {
            log.error("Unable to check if user '{}' exists, returning true", username, e);
            return true;
        }
    }

    /**
     * Returns the User's local memberships.
     *
     * @return the User's local memberships
     */
    public List<Group> getLocalMemberships(final String username) {
        final String escapedUsername = Text.escapeIllegalXpathSearchChars(username).replaceAll("'", "''");
        final String queryString = QUERY_LOCAL_MEMBERSHIPS.replace("{}", escapedUsername);
        final List<Group> localMemberships = new ArrayList<Group>();
        try {
            final Query query = getQueryManager().createQuery(queryString, Query.XPATH);
            final NodeIterator iter = query.execute().getNodes();
            while (iter.hasNext()) {
                final Node node = iter.nextNode();
                if (node == null) {
                    continue;
                }
                try {
                    final Group g = new Group(node.getName());
                    if (node.hasProperty(PROP_DESCRIPTION)) {
                        g.setDescription(node.getProperty(PROP_DESCRIPTION).getString());
                    }
                    if (node.isNodeType(HippoNodeType.NT_EXTERNALGROUP)) {
                        g.setExternal(true);
                    }

                    localMemberships.add(g);
                } catch (RepositoryException e) {
                    log.warn("Unable to add group to local memberships for user '{}'", username, e);
                }
            }
        } catch (RepositoryException e) {
            log.error("Error while querying local memberships of user '{}'", e);
        }
        return localMemberships;
    }

    public List<Group> getLocalMembershipsAsListOfGroups(final String username) {
        List<Group> groups = new ArrayList<Group>();
        for (Group group : getLocalMemberships(username)) {
            groups.add(group);
        }
        return groups;
    }

}

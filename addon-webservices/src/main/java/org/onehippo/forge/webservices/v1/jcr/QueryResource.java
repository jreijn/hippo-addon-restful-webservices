package org.onehippo.forge.webservices.v1.jcr;

import java.net.URI;
import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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
import org.hippoecm.repository.api.HippoNodeIterator;
import org.onehippo.forge.webservices.v1.jcr.model.JcrNode;
import org.onehippo.forge.webservices.v1.jcr.model.JcrQueryResult;
import org.onehippo.forge.webservices.v1.jcr.model.JcrSearchQuery;
import org.onehippo.forge.webservices.v1.jcr.util.JcrDataBindingHelper;
import org.onehippo.forge.webservices.v1.jcr.util.RepositoryConnectionUtils;
import org.onehippo.forge.webservices.v1.jcr.util.ResponseConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(value = "v1/query", description = "API for searching nodes")
@Path("v1/query")
@CrossOriginResourceSharing(allowAllOrigins = true)
public class QueryResource {

    private static Logger log = LoggerFactory.getLogger(QueryResource.class);

    @Context
    private HttpServletRequest request;

    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Query for nodes", notes = "Returns a list of nodes", position = 1)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ResponseConstants.STATUS_MESSAGE_OK, response = JcrQueryResult.class),
            @ApiResponse(code = 401, message = ResponseConstants.STATUS_MESSAGE_UNAUTHORIZED),
            @ApiResponse(code = 404, message = ResponseConstants.STATUS_MESSAGE_NODE_NOT_FOUND),
            @ApiResponse(code = 500, message = ResponseConstants.STATUS_MESSAGE_ERROR_OCCURRED)
    })
    public Response getResults(@ApiParam(required = true, value = "JCR valid query syntax. '//element(*,hippo:document) order by @jcr:score'")
                               @QueryParam("statement") String statement,
                               @ApiParam(required = true, value = "JCR query language e.g 'xpath, sql' or 'JCR-SQL2'")
                               @QueryParam("language") String language,
                               @ApiParam(required = false, value = "Sets the maximum size of the result set.")
                               @QueryParam("limit") @DefaultValue("200") int limit,
                               @ApiParam(required = false, value = "Sets the start offset of the result set.")
                               @QueryParam("offset") int offset,
                               @Context UriInfo ui) {
        JcrQueryResult jcrQueryResult = new JcrQueryResult();
        ArrayList<JcrNode> resultItems = new ArrayList<JcrNode>();

        Session session = null;
        try {
            session = RepositoryConnectionUtils.createSession(request);
            Query jcrQuery = session.getWorkspace().getQueryManager().createQuery(statement, language);
            if (limit > 0) {
                jcrQuery.setLimit(limit);
            }
            if (offset > 0) {
                jcrQuery.setOffset(offset);
            }
            QueryResult queryResult = jcrQuery.execute();
            HippoNodeIterator nodeIterator = (HippoNodeIterator) queryResult.getNodes();
            long totalSize = nodeIterator.getTotalSize();
            if (totalSize == -1) {
                log.error("getTotalSize returned -1 for query. Should not be possible. Fallback to normal getSize()");
                totalSize = nodeIterator.getSize();
            }
            jcrQueryResult.setHits(totalSize);
            while (nodeIterator.hasNext()) {
                Node node = nodeIterator.nextNode();
                final JcrNode nodeRepresentation = JcrDataBindingHelper.getNodeRepresentation(node, 0);
                final URI nodeUri = getUriForNode(ui, nodeRepresentation);
                nodeRepresentation.setLink(nodeUri);
                resultItems.add(nodeRepresentation);
            }
        } catch (RepositoryException e) {
            log.warn("An exception occurred while trying to perform query: {}", e);
            throw new WebApplicationException(e.getCause());
        } finally {
            RepositoryConnectionUtils.cleanupSession(session);
        }

        jcrQueryResult.setNodes(resultItems);
        return Response.ok(jcrQueryResult).build();
    }

    @POST
    @Path("/")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Query for nodes", notes = "Returns a list of nodes. This method is especially useful when the query exceeds the maximum length of the URL.", position = 2)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ResponseConstants.STATUS_MESSAGE_OK, response = JcrQueryResult.class),
            @ApiResponse(code = 401, message = ResponseConstants.STATUS_MESSAGE_UNAUTHORIZED),
            @ApiResponse(code = 404, message = ResponseConstants.STATUS_MESSAGE_NODE_NOT_FOUND),
            @ApiResponse(code = 500, message = ResponseConstants.STATUS_MESSAGE_ERROR_OCCURRED)
    })
    public Response getResults(JcrSearchQuery searchQuery, @Context UriInfo ui) {
        JcrQueryResult jcrQueryResult = new JcrQueryResult();
        ArrayList<JcrNode> resultItems = new ArrayList<JcrNode>();

        Session session = null;
        try {
            session = RepositoryConnectionUtils.createSession(request);
            Query query = session.getWorkspace().getQueryManager().createQuery(searchQuery.getStatement(), searchQuery.getLanguage());
            if (searchQuery.getLimit() > 0) {
                query.setLimit(searchQuery.getLimit());
            }
            if (searchQuery.getOffset() > 0) {
                query.setOffset(searchQuery.getOffset());
            }

            javax.jcr.query.QueryResult queryResult = query.execute();
            HippoNodeIterator nodeIterator = (HippoNodeIterator) queryResult.getNodes();
            jcrQueryResult.setHits(nodeIterator.getTotalSize());
            while (nodeIterator.hasNext()) {
                Node node = nodeIterator.nextNode();
                final JcrNode nodeRepresentation = JcrDataBindingHelper.getNodeRepresentation(node, 0);
                final URI nodeUri = getUriForNode(ui, nodeRepresentation);
                nodeRepresentation.setLink(nodeUri);
                resultItems.add(nodeRepresentation);
            }
        } catch (RepositoryException e) {
            log.warn("An exception occurred while trying to perform query: {}", e);
        } finally {
            RepositoryConnectionUtils.cleanupSession(session);
        }

        jcrQueryResult.setNodes(resultItems);
        return Response.ok(jcrQueryResult).build();
    }

    private URI getUriForNode(final UriInfo ui, final JcrNode nodeRepresentation) {
        UriBuilder uriBuilder = ui.getBaseUriBuilder().path(NodesResource.class).path(NodesResource.class, "getNodeByPath");
        return uriBuilder.build(nodeRepresentation.getPath().substring(1));
    }

}

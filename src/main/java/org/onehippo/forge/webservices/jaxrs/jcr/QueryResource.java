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
import java.util.ArrayList;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
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
import org.onehippo.forge.webservices.jaxrs.jcr.model.JcrNode;
import org.onehippo.forge.webservices.jaxrs.jcr.model.JcrQueryResult;
import org.onehippo.forge.webservices.jaxrs.jcr.model.JcrQueryResultNode;
import org.onehippo.forge.webservices.jaxrs.jcr.model.JcrSearchQuery;
import org.onehippo.forge.webservices.jaxrs.jcr.util.JcrDataBindingHelper;
import org.onehippo.forge.webservices.jaxrs.jcr.util.RepositoryConnectionUtils;
import org.onehippo.forge.webservices.jaxrs.jcr.util.ResponseConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(value = "_query", description = "API for searching nodes",position = 4)
@Path("_query")
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
    public Response getResults(@ApiParam(required = true, value = "JCR valid query syntax. '//element(*,hippo:document) order by @jcr:score descending'")
                               @QueryParam("statement") String statement,
                               @ApiParam(required = true, value = "JCR query language e.g 'xpath, sql' or 'JCR-SQL2'")
                               @QueryParam("language") String language,
                               @ApiParam(required = false, value = "Sets the maximum size of the result set.")
                               @QueryParam("limit") @DefaultValue("200") int limit,
                               @ApiParam(required = false, value = "Sets the start offset of the result set.")
                               @QueryParam("offset") int offset,
                               @Context UriInfo ui) {
        JcrQueryResult jcrQueryResult = new JcrQueryResult();
        ArrayList<JcrQueryResultNode> resultItems = new ArrayList<JcrQueryResultNode>();

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
            final RowIterator rowIterator = queryResult.getRows();
            HippoNodeIterator nodeIterator = (HippoNodeIterator) queryResult.getNodes();
            long totalSize = nodeIterator.getTotalSize();
            jcrQueryResult.setHits(totalSize);
            while (rowIterator.hasNext()) {
                Row row = rowIterator.nextRow();
                final JcrNode nodeRepresentation = JcrDataBindingHelper.getNodeRepresentation(row.getNode(), 0);
                final URI nodeUri = getUriForNode(ui, nodeRepresentation);

                final JcrQueryResultNode queryResultNode = new JcrQueryResultNode();
                queryResultNode.setNode(nodeRepresentation);
                queryResultNode.setLink(nodeUri);
                queryResultNode.setScore(row.getScore());
                resultItems.add(queryResultNode);
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
        ArrayList<JcrQueryResultNode> resultItems = new ArrayList<JcrQueryResultNode>();

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
            final RowIterator rowIterator = queryResult.getRows();
            long totalSize = rowIterator.getSize();
            jcrQueryResult.setHits(totalSize);
            while (rowIterator.hasNext()) {
                Row row = rowIterator.nextRow();
                final JcrNode nodeRepresentation = JcrDataBindingHelper.getNodeRepresentation(row.getNode(), 0);
                final URI nodeUri = getUriForNode(ui, nodeRepresentation);

                final JcrQueryResultNode queryResultNode = new JcrQueryResultNode();
                queryResultNode.setNode(nodeRepresentation);
                queryResultNode.setLink(nodeUri);
                queryResultNode.setScore(row.getScore());
                resultItems.add(queryResultNode);
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

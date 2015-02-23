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

package org.onehippo.forge.webservices.jaxrs;

import java.lang.reflect.Field;

import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.apache.jackrabbit.api.stats.QueryStat;
import org.apache.jackrabbit.api.stats.QueryStatDto;
import org.apache.jackrabbit.api.stats.RepositoryStatistics;
import org.apache.jackrabbit.api.stats.TimeSeries;
import org.apache.jackrabbit.core.RepositoryContext;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.stats.QueryStatCore;
import org.apache.jackrabbit.core.stats.RepositoryStatisticsImpl;
import org.hippoecm.repository.impl.RepositoryDecorator;
import org.onehippo.forge.webservices.jaxrs.jcr.util.JcrSessionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource for showing repository statistics
 *
 * @see "http://wiki.apache.org/jackrabbit/Statistics" for more information
 */
@Api(value = "v1/_stats", description = "Statistics API.", position = 5)
@Path(value = "v1/_stats")
@CrossOriginResourceSharing(allowAllOrigins = true)
public class StatsResource {

    @Context
    private HttpServletRequest request;

    private RepositoryStatisticsImpl statistics;

    private static final Logger log = LoggerFactory.getLogger(StatsResource.class);

    @ApiOperation(
            value = "Display all statistics about the repository instance; nr of sessions, queries, bundle operations",
            notes = "",
            position = 1)
    @GET
    @Path("/repository")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInstanceInformation() {
        Session session = JcrSessionUtil.getSessionFromRequest(request);
        RepositoryStatistics repositoryStatistics = getRepositoryStatistics(session);
        return Response.ok(repositoryStatistics).build();
    }

    @ApiOperation(
            value = "Display specific statistics based on the key about the repository instance",
            notes = "",
            position = 2)
    @GET
    @Path("/repository/type/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInstanceInformationByKey(@PathParam("key") String key) {
        TimeSeries timeSeries = null;
        Session session = JcrSessionUtil.getSessionFromRequest(request);
        RepositoryStatistics repositoryStatistics = getRepositoryStatistics(session);
        timeSeries = repositoryStatistics.getTimeSeries(RepositoryStatistics.Type.getType(key));
        return Response.ok(timeSeries).build();
    }

    @ApiOperation(
            value = "Displays the most popular queries",
            notes = "Query stats collection needs to be enabled",
            position = 3)
    @GET
    @Path("/queries/popular")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPopularQueries() {
        QueryStatDto[] popularQueries = null;
        try {
            Session session = JcrSessionUtil.getSessionFromRequest(request);
            final QueryStat queryStatistics = getQueryStatistics(session);
            popularQueries = queryStatistics.getPopularQueries();
        } catch (IllegalAccessException e) {
            log.warn("An exception occurred while trying to get query information: {}", e);
        } catch (NoSuchFieldException e) {
            log.warn("An exception occurred while trying to get query information: {}", e);
        }
        return Response.ok(popularQueries).build();
    }

    @ApiOperation(
            value = "Displays the slowest queries",
            notes = "Query stats collection needs to be enabled",
            position = 4)
    @GET
    @Path("/queries/slow")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSlowQueries() {
        QueryStatDto[] slowQueries = null;
        try {
            Session session = JcrSessionUtil.getSessionFromRequest(request);
            final QueryStat queryStatistics = getQueryStatistics(session);
            slowQueries = queryStatistics.getSlowQueries();
        } catch (IllegalAccessException e) {
            log.warn("An exception occurred while trying to get query information: {}", e);
        } catch (NoSuchFieldException e) {
            log.warn("An exception occurred while trying to get query information: {}", e);
        }
        return Response.ok(slowQueries).build();
    }

    private QueryStat getQueryStatistics(final Session session) throws NoSuchFieldException, IllegalAccessException {
        final RepositoryContext repositoryContext = getRepositoryContext(session);
        final QueryStatCore queryStat = repositoryContext.getStatManager().getQueryStat();
        return queryStat;
    }

    private RepositoryStatistics getRepositoryStatistics(Session session) {
        if (statistics == null) {
            try {
                RepositoryContext repositoryContext = getRepositoryContext(session);
                this.statistics = repositoryContext.getRepositoryStatistics();
            } catch (SecurityException e) {
                throw new IllegalArgumentException(e);
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException(e);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(e);
            }
            return statistics;
        } else {
            return statistics;
        }
    }

    private RepositoryContext getRepositoryContext(final Session session) throws NoSuchFieldException, IllegalAccessException {
        Field contextField = RepositoryImpl.class.getDeclaredField("context");
        if (!contextField.isAccessible()) {
            contextField.setAccessible(true);
        }
        return (RepositoryContext) contextField.get(RepositoryDecorator.unwrap(session.getRepository()));
    }

}

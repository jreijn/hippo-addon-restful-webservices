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

import javax.jcr.LoginException;
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
import org.apache.jackrabbit.api.stats.RepositoryStatistics;
import org.apache.jackrabbit.api.stats.TimeSeries;
import org.apache.jackrabbit.core.RepositoryContext;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.stats.RepositoryStatisticsImpl;
import org.hippoecm.repository.impl.RepositoryDecorator;
import org.onehippo.forge.webservices.jaxrs.jcr.util.RepositoryConnectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource for showing statistics http://wiki.apache.org/jackrabbit/Statistics
 *
 * @author Jeroen Reijn
 */
@Api(value = "_stats", description = "Statistics API.", position = 5)
@Path(value = "_stats")
@CrossOriginResourceSharing(allowAllOrigins = true)
public class StatsResource {

    @Context
    private HttpServletRequest request;

    private RepositoryStatisticsImpl statistics;

    private static Logger log = LoggerFactory.getLogger(StatsResource.class);

    @ApiOperation(
            value = "Display all statistics about the repository instance; nr of sessions, queries, bundle operations",
            notes = "",
            position = 1)
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInstanceInformation() {
        Session session = null;
        RepositoryStatistics repositoryStatistics = null;
        try {
            session = RepositoryConnectionUtils.createSession(request);
            repositoryStatistics = getStatistics(session);
        } catch (LoginException e) {
            log.warn("An exception occurred while trying to login: {}", e);
        } finally {
            RepositoryConnectionUtils.cleanupSession(session);
        }
        return Response.ok(repositoryStatistics).build();
    }

    @ApiOperation(
            value = "Display specific statistics based on the key about the repository instance",
            notes = "",
            position = 2)
    @GET
    @Path("/type/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInstanceInformationByKey(@PathParam("key") String key) {
        Session session = null;
        TimeSeries timeSeries = null;
        try {
            session = RepositoryConnectionUtils.createSession(request);
            RepositoryStatistics repositoryStatistics = getStatistics(session);
            timeSeries = repositoryStatistics.getTimeSeries(RepositoryStatistics.Type.getType(key));
        } catch (LoginException e) {
            log.warn("An exception occurred while trying to login: {}", e);
        } finally {
            RepositoryConnectionUtils.cleanupSession(session);
        }
        return Response.ok(timeSeries).build();
    }


    private RepositoryStatistics getStatistics(Session session) {
        if (statistics == null) {
            try {
                Field contextField = RepositoryImpl.class.getDeclaredField("context");
                if (!contextField.isAccessible()) {
                    contextField.setAccessible(true);
                }
                RepositoryContext repositoryContext = (RepositoryContext) contextField.get(RepositoryDecorator.unwrap(session.getRepository()));
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

}

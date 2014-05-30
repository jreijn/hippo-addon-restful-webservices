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

import java.util.LinkedHashMap;
import java.util.Map;

import javax.jcr.LoginException;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.onehippo.forge.webservices.jaxrs.jcr.util.RepositoryConnectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeroen Reijn
 */
@Api(value = "/", description = "API Root.", position = 1)
@Path(value = "/")
@CrossOriginResourceSharing(allowAllOrigins = true)
public class RootResource {

    private static Logger log = LoggerFactory.getLogger(RootResource.class);

    @Context
    private HttpServletRequest request;

    @ApiOperation(
            value = "Display basic information about the repository instance",
            notes = "",
            position = 1)
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInstanceInformation() {
        Session session = null;
        Map<String, Object> info = new LinkedHashMap<String, Object>();
        Map<String, Object> vendor = new LinkedHashMap<String, Object>();
        try {
            session = RepositoryConnectionUtils.createSession(request);
            info.put("clusterid", getClusterNodeId(session));
            vendor.put("name", getRepositoryVendor(session));
            vendor.put("version", getRepositoryVersion(session));
            info.put("vendor",vendor);
        } catch (LoginException e) {
            log.warn("An exception occurred while trying to login: {}", e);
        } finally {
            RepositoryConnectionUtils.cleanupSession(session);
        }
        return Response.ok(info).build();
    }

    private String getClusterNodeId(Session session) {
        String clusterNodeId = "";
        if (session.getRepository() != null) {
            final String clusterId = session.getRepository().getDescriptor("jackrabbit.cluster.id");
            if(clusterId!=null){
                clusterNodeId = clusterId;
            }
        }
        return clusterNodeId;
    }

    private String getRepositoryVersion(Session session) {
        Repository repository = session.getRepository();
        if (repository != null) {
            return repository.getDescriptor(Repository.REP_VERSION_DESC);
        } else {
            return "unknown";
        }
    }

    private String getRepositoryVendor(Session session) {
        Repository repository = session.getRepository();
        if (repository != null) {
            return repository.getDescriptor(Repository.REP_NAME_DESC);
        } else {
            return "unknown";
        }
    }
}

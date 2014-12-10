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

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.onehippo.forge.webservices.jaxrs.jcr.util.JcrSessionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The root resource which will be there for consumers to go to a default api endpoint.
 */
@Path(value = "/")
@CrossOriginResourceSharing(allowAllOrigins = true)
public class RootResource {

    private static final Logger log = LoggerFactory.getLogger(RootResource.class);
    public static final String UNKNOWN = "unknown";

    @Context
    private HttpServletRequest request;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInstanceInformation() {
        Map<String, Object> info = new LinkedHashMap<String, Object>();
        Map<String, Object> vendor = new LinkedHashMap<String, Object>();
        Session session = JcrSessionUtil.getSessionFromRequest(request);
        info.put("clusterid", getClusterNodeId(session));
        vendor.put("name", getRepositoryVendor(session));
        vendor.put("version", getRepositoryVersion(session));
        info.put("vendor", vendor);
        return Response.ok(info).build();
    }

    private String getClusterNodeId(Session session) {
        String emptyClusterNodeId = "";
        if (session.getRepository() != null) {
            final String clusterId = session.getRepository().getDescriptor("jackrabbit.cluster.id");
            if (StringUtils.isNotBlank(clusterId)) {
                return clusterId;
            }
        }
        return emptyClusterNodeId;
    }

    private String getRepositoryVersion(Session session) {
        Repository repository = session.getRepository();
        if (repository != null) {
            return repository.getDescriptor(Repository.REP_VERSION_DESC);
        } else {
            return UNKNOWN;
        }
    }

    private String getRepositoryVendor(Session session) {
        Repository repository = session.getRepository();
        if (repository != null) {
            return repository.getDescriptor(Repository.REP_NAME_DESC);
        } else {
            return UNKNOWN;
        }
    }
}

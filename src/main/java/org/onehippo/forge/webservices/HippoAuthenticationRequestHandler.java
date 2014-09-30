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

package org.onehippo.forge.webservices;

import javax.jcr.LoginException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.onehippo.forge.webservices.jaxrs.exception.UnauthorizedException;
import org.onehippo.forge.webservices.jaxrs.jcr.util.RepositoryConnectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class HippoAuthenticationRequestHandler implements RequestHandler {

    private static final Logger log = LoggerFactory.getLogger(HippoAuthenticationRequestHandler.class);

    public Response handleRequest(Message m, ClassResourceInfo resourceClass) {
        AuthorizationPolicy policy = m.get(AuthorizationPolicy.class);
        if (policy != null) {
            String username = policy.getUserName();
            String password = policy.getPassword();
            Session session = null;
            try {
                session = RepositoryConnectionUtils.createSession(username, password);
                if (isAuthenticated(session)) {
                    HttpServletRequest request = (HttpServletRequest) m.get(AbstractHTTPDestination.HTTP_REQUEST);
                    request.setAttribute(AuthenticationConstants.HIPPO_CREDENTIALS, new SimpleCredentials(username, password.toCharArray()));
                    return null;
                } else {
                    throw new UnauthorizedException();
                }
            } catch (LoginException e) {
                log.debug("Login failed: {}", e);
                throw new UnauthorizedException(e.getMessage());
            } finally {
                RepositoryConnectionUtils.cleanupSession(session);
            }
        }
        throw new UnauthorizedException();
    }

    private boolean isAuthenticated(Session session) {
        return session != null;
    }
}
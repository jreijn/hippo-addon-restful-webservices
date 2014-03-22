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
import org.onehippo.forge.webservices.v1.jcr.util.RepositoryConnectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class HippoAuthenticationRequestHandler implements RequestHandler {

    private static Logger log = LoggerFactory.getLogger(HippoAuthenticationRequestHandler.class);
    private static String WWW_AUTHENTICATE_HEADER_VALUE = "Basic realm=\"Default realm\"";
    private static String WWW_AUTHENTICATE_HEADER_NAME = "WWW-Authenticate";

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
                    // authentication failed, request the authentication, add the realm name if needed to the value of WWW-Authenticate
                    return Response.status(401).header(WWW_AUTHENTICATE_HEADER_NAME, WWW_AUTHENTICATE_HEADER_VALUE).build();
                }
            } catch (LoginException e) {
                log.debug("Login failed: {}", e);
                throw new UnauthorizedException(e.getMessage(), "Hippo API Realm");
            } finally {
                RepositoryConnectionUtils.cleanupSession(session);
            }
        }
        return Response.status(401).header("WWW-Authenticate", "Basic").build();
    }

    private boolean isAuthenticated(Session session) {
        if (session != null) {
            return true;
        }
        return false;
    }
}
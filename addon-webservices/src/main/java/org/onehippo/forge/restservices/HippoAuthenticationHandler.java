package org.onehippo.forge.restservices;

import javax.jcr.Session;
import javax.ws.rs.core.Response;

import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.onehippo.forge.restservices.v1.jcr.RepositoryConnectionUtils;

public class HippoAuthenticationHandler implements RequestHandler {

    public Response handleRequest(Message m, ClassResourceInfo resourceClass) {
        AuthorizationPolicy policy = (AuthorizationPolicy)m.get(AuthorizationPolicy.class);
        String username = policy.getUserName();
        String password = policy.getPassword(); 
        if (isAuthenticated(username, password)) {
            // let request to continue
            return null;
        } else {
            // authentication failed, request the authetication, add the realm name if needed to the value of WWW-Authenticate 
            return Response.status(401).header("WWW-Authenticate", "Basic").build();
        }
    }

    private boolean isAuthenticated(final String username, final String password) {
        final Session session = RepositoryConnectionUtils.createSession(username, password);
        if(session!=null){
            return true;
        }
        return false;
    }

}
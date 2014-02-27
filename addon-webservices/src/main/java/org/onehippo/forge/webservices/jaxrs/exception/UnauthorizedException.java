package org.onehippo.forge.webservices.jaxrs.exception;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.*;
 
/**
 * Throw this exception to return a 401 Unauthorized response.
 * The WWW-Authenticate header is set appropriately and a short message is included in the response entity.
 **/
public class UnauthorizedException extends WebApplicationException
{
    private static final long serialVersionUID = 1L;
 
    public UnauthorizedException()
    {
        this("Please authenticate.", "Name of your web service");
    }
 
    public UnauthorizedException(String message, String realm)
    {
        super(Response.status(Status.UNAUTHORIZED).header(HttpHeaders.WWW_AUTHENTICATE,
                                                          "Basic realm=\"" + realm + "\"")
                .entity(message).build());
    }
}
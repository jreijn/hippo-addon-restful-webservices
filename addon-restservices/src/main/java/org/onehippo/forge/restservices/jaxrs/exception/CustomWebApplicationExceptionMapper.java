package org.onehippo.forge.restservices.jaxrs.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps {@link WebApplicationException}'s to an exception representation and response code. This exception mapper is
 * used to represent a response when any exception occurs within the service.
 *
 * @author Jeroen Reijn
 */
public class CustomWebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    private static Logger log = LoggerFactory.getLogger(CustomWebApplicationExceptionMapper.class);

    @Override
    public Response toResponse(WebApplicationException ex) {
        String message = ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage();
        int status = ex.getResponse().getStatus();
        if (message == null) {
            if (ex.getCause() != null) {
                message = "cause is " + ex.getCause().getClass().getName();
            } else {
                message = "no cause is available";
            }
        }

        message = deriveStatusMessage(message, status);

        ResponseExceptionRepresentation errorRepresentation = new ResponseExceptionRepresentation();
        errorRepresentation.setMessage(message);
        errorRepresentation.setStatusCode(status);
        return Response.ok(errorRepresentation).status(status).build();
    }

    private String deriveStatusMessage(final String message, final int statusCode) {

        Response.Status status = Response.Status.fromStatusCode(statusCode);

        if (status == null) {
            return message;
        }

        switch (status) {
            case BAD_REQUEST:
            case NOT_FOUND:
            case INTERNAL_SERVER_ERROR:
            case NOT_ACCEPTABLE:
                return status.getReasonPhrase();
            default:
                return message;
        }
    }
}
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

package org.onehippo.forge.webservices.jaxrs.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps {@link WebApplicationException}'s to an exception representation and response code. This exception mapper is
 * used to represent a response when any exception occurs within the service.
 */
@Provider
public class CustomWebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

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
        return Response.status(status).entity(errorRepresentation).type(MediaType.APPLICATION_JSON).build();
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
            case UNAUTHORIZED:
                return status.getReasonPhrase();
            default:
                return message;
        }
    }
}
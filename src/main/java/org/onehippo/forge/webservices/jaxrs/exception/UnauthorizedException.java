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

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.*;
 
/**
 * Throw this exception to return a 401 Unauthorized response.
 * The WWW-Authenticate header is set appropriately and a short message is included in the response entity.
 **/
public class UnauthorizedException extends WebApplicationException {

    private static final long serialVersionUID = 1L;
    private static String WWW_AUTHENTICATE_HEADER_VALUE = "Basic realm=\"Default realm\"";

    public UnauthorizedException() {
        this("Please authenticate.");
    }
 
    public UnauthorizedException(String message) {
        super(Response.status(Status.UNAUTHORIZED).header(HttpHeaders.WWW_AUTHENTICATE,
                WWW_AUTHENTICATE_HEADER_VALUE)
                .entity(message).build());
    }
}
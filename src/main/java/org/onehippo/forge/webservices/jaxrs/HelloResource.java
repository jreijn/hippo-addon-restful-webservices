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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;

/**
 * Api for hello world from Hippo
 *
 * @author Jeroen Reijn
 */
@Api(value = "hello", description = "API for Hello World from Hippo")
@Path(value = "hello")
@CrossOriginResourceSharing(allowAllOrigins = true)
public class HelloResource {

    @ApiOperation(
            value = "Displays an hello world message",
            notes = "",
            position = 1)
    @Path(value = "/")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getHelloWorld() {
        Map<String, String> messages = new LinkedHashMap<String, String>();
        messages.put("message", "Hello World from Hippo CMS");
        return Response.ok(messages).build();
    }

}

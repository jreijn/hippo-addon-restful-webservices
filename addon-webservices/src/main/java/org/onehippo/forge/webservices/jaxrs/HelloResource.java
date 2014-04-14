package org.onehippo.forge.webservices.jaxrs;

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
        return Response.ok("Hello World from Hippo CMS").build();
    }

}

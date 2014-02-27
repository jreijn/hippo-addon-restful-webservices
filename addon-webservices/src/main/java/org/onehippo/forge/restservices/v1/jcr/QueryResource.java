package org.onehippo.forge.restservices.v1.jcr;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.wordnik.swagger.annotations.Api;

@Api(value = "v1/query", description = "API for searching nodes")
@Path("v1/query")
public class QueryResource {

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    public Response getResults() {
        return Response.ok().build();
    }

}

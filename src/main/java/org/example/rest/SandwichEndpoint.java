package org.example.rest;

import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
@RequestScoped
public class SandwichEndpoint
{
    @Inject
    private SandwichDAO sandwichDAO;

    @GET
    @Path("/test")
    @Produces(MediaType.APPLICATION_JSON)
    public Response test() {
        List<String> entity = sandwichDAO.getIngredients();
        return Response.status(Response.Status.OK).entity(entity).build();
    }
}

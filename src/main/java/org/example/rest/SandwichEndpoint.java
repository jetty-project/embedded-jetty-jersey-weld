package org.example.rest;

import java.util.List;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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

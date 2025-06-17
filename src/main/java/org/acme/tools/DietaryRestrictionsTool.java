package org.acme.tools;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/tool")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class DietaryRestrictionsTool {
    @GET
    @Path("/dietary-restrictions")
    public Response getDietaryRestrictions() {
        return Response.ok(List.of(
                "pistache",
                "almonds"
        )).build();
    }
}

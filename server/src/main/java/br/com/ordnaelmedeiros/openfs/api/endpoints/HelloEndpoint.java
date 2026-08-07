package br.com.ordnaelmedeiros.openfs.api.endpoints;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/hello")
public interface HelloEndpoint {

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  String hello();
}

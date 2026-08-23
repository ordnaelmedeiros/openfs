package br.com.ordnaelmedeiros.openfs.api.endpoints;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/status/now")
public interface StatusEndpoint {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  StatusResponse now();

  record StatusResponse(String now) {}
}

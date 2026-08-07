package br.com.ordnaelmedeiros.openfs.s3.endpoints;

import br.com.ordnaelmedeiros.openfs.domain.status.StatusResource;
import br.com.ordnaelmedeiros.openfs.s3.server.S3Endpoint;
import br.com.ordnaelmedeiros.openfs.s3.server.S3ResponseWriter;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.mutiny.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class StatusNowEndpoint implements S3Endpoint {

  @Inject
  StatusResource statusResource;

  @Inject
  S3ResponseWriter responseWriter;

  @Override
  public Request request() {
    return new Request(HttpMethod.GET, "/status/now");
  }

  @Override
  public Uni<Void> handle(RoutingContext ctx) {
    return Uni.createFrom().item(() -> statusResource.now())
      .onItem().transformToUni(status -> responseWriter.write(ctx, status));
  }
}

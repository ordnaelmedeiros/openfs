package br.com.ordnaelmedeiros.openfs.s3.endpoints;

import br.com.ordnaelmedeiros.openfs.domain.bucket.BucketStorageService;
import br.com.ordnaelmedeiros.openfs.s3.server.S3Endpoint;
import br.com.ordnaelmedeiros.openfs.s3.server.S3ResponseWriter;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.mutiny.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class HeadBucketEndpoint implements S3Endpoint {

  @Inject
  BucketStorageService bucketStorage;

  @Inject
  S3ResponseWriter responseWriter;

  @Override
  public Request request() {
    return new Request(HttpMethod.HEAD, "/:bucket");
  }

  @Override
  public Uni<Void> handle(RoutingContext ctx) {
    String bucketName = ctx.pathParam("bucket");

    try {
      if (bucketStorage.bucketExists(bucketName)) {
        ctx.response().setStatusCode(200);
      } else {
        ctx.response().setStatusCode(404);
      }
      return responseWriter.write(ctx, null);
    } catch (IllegalArgumentException e) {
      ctx.response().setStatusCode(404);
      return responseWriter.write(ctx, null);
    }
  }
}

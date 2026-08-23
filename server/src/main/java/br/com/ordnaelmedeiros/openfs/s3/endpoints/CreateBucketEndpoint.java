package br.com.ordnaelmedeiros.openfs.s3.endpoints;

import br.com.ordnaelmedeiros.openfs.domain.bucket.BucketStorageService;
import br.com.ordnaelmedeiros.openfs.s3.server.S3Endpoint;
import br.com.ordnaelmedeiros.openfs.s3.server.S3ResponseWriter;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.mutiny.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CreateBucketEndpoint implements S3Endpoint {

  @Inject
  BucketStorageService bucketStorage;

  @Inject
  S3ResponseWriter responseWriter;

  @Override
  public Request request() {
    return new Request(HttpMethod.PUT, "/:bucket");
  }

  @Override
  public Uni<Void> handle(RoutingContext ctx) {
    String bucketName = ctx.pathParam("bucket");

    try {
      if (bucketStorage.bucketExists(bucketName)) {
        ctx.response().setStatusCode(409);
        return responseWriter.write(ctx, "BucketAlreadyExists");
      }

      bucketStorage.createBucket(bucketName);
      ctx.response().putHeader(HttpHeaders.LOCATION, "/" + bucketName);
      ctx.response().setStatusCode(200);
      return responseWriter.write(ctx, null);
    } catch (IllegalArgumentException e) {
      Log.warnf("Invalid bucket name: %s - %s", bucketName, e.getMessage());
      ctx.response().setStatusCode(400);
      return responseWriter.write(ctx, "Invalid bucket name: " + e.getMessage());
    } catch (Exception e) {
      Log.errorf(e, "Failed to create bucket: %s", bucketName);
      ctx.response().setStatusCode(500);
      return responseWriter.write(ctx, "Error creating bucket: " + e.getMessage());
    }
  }
}

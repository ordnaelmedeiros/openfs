package br.com.ordnaelmedeiros.openfs.s3.endpoints;

import br.com.ordnaelmedeiros.openfs.domain.bucket.BucketStorageService;
import br.com.ordnaelmedeiros.openfs.s3.server.S3Endpoint;
import br.com.ordnaelmedeiros.openfs.s3.server.S3ResponseWriter;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.mutiny.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.NoSuchFileException;

@ApplicationScoped
public class DeleteBucketEndpoint implements S3Endpoint {

  @Inject
  BucketStorageService bucketStorage;

  @Inject
  S3ResponseWriter responseWriter;

  @Override
  public Request request() {
    return new Request(HttpMethod.DELETE, "/:bucket");
  }

  @Override
  public Uni<Void> handle(RoutingContext ctx) {
    String bucketName = ctx.pathParam("bucket");

    try {
      bucketStorage.deleteBucket(bucketName);
      ctx.response().setStatusCode(204);
      return responseWriter.write(ctx, null);
    } catch (NoSuchFileException e) {
      Log.warnf("Bucket not found: %s", bucketName);
      ctx.response().setStatusCode(404);
      return responseWriter.write(ctx, "NoSuchBucket");
    } catch (DirectoryNotEmptyException e) {
      Log.warnf("Bucket not empty: %s", bucketName);
      ctx.response().setStatusCode(409);
      return responseWriter.write(ctx, "BucketNotEmpty");
    } catch (IllegalArgumentException e) {
      Log.warnf("Invalid bucket name: %s - %s", bucketName, e.getMessage());
      ctx.response().setStatusCode(400);
      return responseWriter.write(ctx, "Invalid bucket name: " + e.getMessage());
    } catch (Exception e) {
      Log.errorf(e, "Failed to delete bucket: %s", bucketName);
      ctx.response().setStatusCode(500);
      return responseWriter.write(ctx, "Error deleting bucket: " + e.getMessage());
    }
  }
}

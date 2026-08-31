package br.com.ordnaelmedeiros.openfs.s3.endpoints;

import br.com.ordnaelmedeiros.openfs.domain.bucket.BucketInfo;
import br.com.ordnaelmedeiros.openfs.domain.bucket.BucketStorageService;
import br.com.ordnaelmedeiros.openfs.s3.server.S3Endpoint;
import br.com.ordnaelmedeiros.openfs.s3.server.S3ResponseWriter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.quarkus.logging.Log;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.mutiny.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.List;

@ApplicationScoped
public class ListBucketsEndpoint implements S3Endpoint {

  private static final String OWNER_ID = "openfs";
  private static final String OWNER_DISPLAY_NAME = "openfs";

  @Inject
  BucketStorageService bucketStorage;

  @Inject
  S3ResponseWriter responseWriter;

  @Override
  public Request request() {
    return new Request(HttpMethod.GET, "/");
  }

  @Override
  public Uni<Void> handle(RoutingContext ctx) {
    try {
      List<BucketInfo> buckets = bucketStorage.listBuckets();
      ListAllMyBucketsResult result = new ListAllMyBucketsResult(
        new Owner(OWNER_ID, OWNER_DISPLAY_NAME),
        buckets.stream()
          .map(bucket -> new Bucket(bucket.name(), formatCreationDate(bucket.creationDate())))
          .toList());
      ctx.response().setStatusCode(200);
      return responseWriter.write(ctx, result);
    } catch (Exception e) {
      Log.errorf(e, "Failed to list buckets");
      ctx.response().setStatusCode(500);
      return responseWriter.write(ctx, "Error listing buckets: " + e.getMessage());
    }
  }

  private String formatCreationDate(Instant creationDate) {
    return DateTimeFormatter.ISO_INSTANT.format(creationDate.truncatedTo(ChronoUnit.MILLIS));
  }

  @RegisterForReflection
  @JacksonXmlRootElement(localName = "ListAllMyBucketsResult")
  record ListAllMyBucketsResult(
    @JacksonXmlProperty(localName = "Owner") Owner owner,
    @JacksonXmlElementWrapper(localName = "Buckets")
    @JacksonXmlProperty(localName = "Bucket") List<Bucket> buckets) {}

  @RegisterForReflection
  record Owner(
    @JacksonXmlProperty(localName = "ID") String id,
    @JacksonXmlProperty(localName = "DisplayName") String displayName) {}

  @RegisterForReflection
  record Bucket(
    @JacksonXmlProperty(localName = "Name") String name,
    @JacksonXmlProperty(localName = "CreationDate") String creationDate) {}
}

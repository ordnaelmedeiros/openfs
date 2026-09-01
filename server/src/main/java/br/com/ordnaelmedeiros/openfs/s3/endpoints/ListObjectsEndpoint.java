package br.com.ordnaelmedeiros.openfs.s3.endpoints;

import br.com.ordnaelmedeiros.openfs.domain.object.ObjectInfo;
import br.com.ordnaelmedeiros.openfs.domain.object.ObjectStorageService;
import br.com.ordnaelmedeiros.openfs.s3.server.S3Endpoint;
import br.com.ordnaelmedeiros.openfs.s3.server.S3ResponseWriter;
import com.fasterxml.jackson.annotation.JsonInclude;
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
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@ApplicationScoped
public class ListObjectsEndpoint implements S3Endpoint {

  private static final String STORAGE_CLASS = "STANDARD";
  private static final String EMPTY_ETAG = "";

  @Inject
  ObjectStorageService objectStorage;

  @Inject
  S3ResponseWriter responseWriter;

  @Override
  public Request request() {
    return new Request(HttpMethod.GET, "/:bucket");
  }

  @Override
  public Uni<Void> handle(RoutingContext ctx) {
    String bucketName = ctx.pathParam("bucket");
    var params = ctx.request().params();
    String prefix = emptyToNull(params.get("prefix"));
    String delimiter = emptyToNull(params.get("delimiter"));
    Integer maxKeys = parseMaxKeys(params.get("max-keys"));
    boolean v2 = "2".equals(params.get("list-type"));

    try {
      ObjectStorageService.ObjectListing listing = objectStorage.listObjects(bucketName, prefix, delimiter, maxKeys);
      ctx.response().setStatusCode(200);
      return responseWriter.write(ctx, toResult(bucketName, prefix, delimiter, listing, v2));
    } catch (ObjectStorageService.NoSuchBucketException | IllegalArgumentException e) {
      Log.warnf("Bucket not found: %s", bucketName);
      ctx.response().setStatusCode(404);
      return responseWriter.write(ctx, new S3Error("NoSuchBucket", "The specified bucket does not exist", "/" + bucketName));
    } catch (Exception e) {
      Log.errorf(e, "Failed to list objects in bucket: %s", bucketName);
      ctx.response().setStatusCode(500);
      return responseWriter.write(ctx, "Error listing objects: " + e.getMessage());
    }
  }

  private Object toResult(String bucketName, String prefix, String delimiter,
      ObjectStorageService.ObjectListing listing, boolean v2) {
    List<Content> contents = listing.contents().stream()
      .map(info -> new Content(info.key(), formatLastModified(info.lastModified()), EMPTY_ETAG, info.size(), STORAGE_CLASS))
      .toList();
    List<CommonPrefix> commonPrefixes = listing.commonPrefixes().stream()
      .map(CommonPrefix::new)
      .toList();
    Integer keyCount = v2 ? listing.keyCount() : null;
    return new ListBucketResult(
      bucketName,
      prefix,
      delimiter,
      listing.maxKeys(),
      keyCount,
      listing.isTruncated(),
      contents,
      commonPrefixes);
  }

  private String formatLastModified(Instant lastModified) {
    return DateTimeFormatter.ISO_INSTANT.format(lastModified.truncatedTo(ChronoUnit.MILLIS));
  }

  private String emptyToNull(String value) {
    return value == null || value.isEmpty() ? null : value;
  }

  private Integer parseMaxKeys(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  @RegisterForReflection
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JacksonXmlRootElement(localName = "ListBucketResult")
  record ListBucketResult(
    @JacksonXmlProperty(localName = "Name") String name,
    @JacksonXmlProperty(localName = "Prefix") String prefix,
    @JacksonXmlProperty(localName = "Delimiter") String delimiter,
    @JacksonXmlProperty(localName = "MaxKeys") int maxKeys,
    @JacksonXmlProperty(localName = "KeyCount") Integer keyCount,
    @JacksonXmlProperty(localName = "IsTruncated") boolean isTruncated,
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "Contents") List<Content> contents,
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "CommonPrefixes") List<CommonPrefix> commonPrefixes) {}

  @RegisterForReflection
  record Content(
    @JacksonXmlProperty(localName = "Key") String key,
    @JacksonXmlProperty(localName = "LastModified") String lastModified,
    @JacksonXmlProperty(localName = "ETag") String etag,
    @JacksonXmlProperty(localName = "Size") long size,
    @JacksonXmlProperty(localName = "StorageClass") String storageClass) {}

  @RegisterForReflection
  record CommonPrefix(
    @JacksonXmlProperty(localName = "Prefix") String prefix) {}

  @RegisterForReflection
  @JacksonXmlRootElement(localName = "Error")
  record S3Error(
    @JacksonXmlProperty(localName = "Code") String code,
    @JacksonXmlProperty(localName = "Message") String message,
    @JacksonXmlProperty(localName = "Resource") String resource) {}
}

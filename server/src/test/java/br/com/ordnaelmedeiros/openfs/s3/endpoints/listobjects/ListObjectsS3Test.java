package br.com.ordnaelmedeiros.openfs.s3.endpoints.listobjects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.ordnaelmedeiros.openfs.testutils.BaseResourceTestS3;
import br.com.ordnaelmedeiros.openfs.testutils.OpenFsContainerExtension;
import br.com.ordnaelmedeiros.openfs.testutils.S3ClientFactory;
import br.com.ordnaelmedeiros.openfs.testutils.Target;
import br.com.ordnaelmedeiros.openfs.testutils.TargetProvider;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

import java.util.UUID;

@QuarkusTest
@ExtendWith(OpenFsContainerExtension.class)
class ListObjectsS3Test extends BaseResourceTestS3 {

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(TargetProvider.class)
  void testListObjectsV2OnEmptyBucket(Target target) {
    String bucketName = "list-objects-s3-" + UUID.randomUUID();

    try (S3Client s3 = S3ClientFactory.create(baseUrl(target))) {
      s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());

      var response = assertDoesNotThrow(() ->
        s3.listObjectsV2(ListObjectsV2Request.builder().bucket(bucketName).build()));

      assertEquals(bucketName, response.name());
      assertTrue(response.contents().isEmpty(), "Empty bucket should have no contents");
      assertTrue(response.commonPrefixes().isEmpty(), "Empty bucket should have no common prefixes");
      assertEquals(0, response.keyCount());
      assertFalse(response.isTruncated());
    }
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(TargetProvider.class)
  void testListObjectsV2WithPrefixAndDelimiterOnEmptyBucket(Target target) {
    String bucketName = "list-objects-s3-filtered-" + UUID.randomUUID();

    try (S3Client s3 = S3ClientFactory.create(baseUrl(target))) {
      s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());

      var response = assertDoesNotThrow(() -> s3.listObjectsV2(ListObjectsV2Request.builder()
        .bucket(bucketName)
        .prefix("dir/")
        .delimiter("/")
        .build()));

      assertTrue(response.contents().isEmpty());
      assertTrue(response.commonPrefixes().isEmpty());
      assertEquals(0, response.keyCount());
      assertEquals("dir/", response.prefix());
      assertEquals("/", response.delimiter());
    }
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(TargetProvider.class)
  void testListObjectsV1OnEmptyBucket(Target target) {
    String bucketName = "list-objects-s3-v1-" + UUID.randomUUID();

    try (S3Client s3 = S3ClientFactory.create(baseUrl(target))) {
      s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());

      var response = assertDoesNotThrow(() ->
        s3.listObjects(ListObjectsRequest.builder().bucket(bucketName).build()));

      assertEquals(bucketName, response.name());
      assertTrue(response.contents().isEmpty(), "Empty bucket should have no contents");
      assertFalse(response.isTruncated());
    }
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(TargetProvider.class)
  void testListObjectsV2OnMissingBucketThrowsNoSuchBucket(Target target) {
    String bucketName = "list-objects-missing-" + UUID.randomUUID();

    try (S3Client s3 = S3ClientFactory.create(baseUrl(target))) {
      assertThrows(NoSuchBucketException.class, () ->
        s3.listObjectsV2(ListObjectsV2Request.builder().bucket(bucketName).build()));
    }
  }
}

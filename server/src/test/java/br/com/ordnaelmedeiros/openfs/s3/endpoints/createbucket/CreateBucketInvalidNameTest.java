package br.com.ordnaelmedeiros.openfs.s3.endpoints.createbucket;

import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.ordnaelmedeiros.openfs.config.OpenFsConfig;
import br.com.ordnaelmedeiros.openfs.testutils.S3ClientFactory;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

@QuarkusTest
class CreateBucketInvalidNameTest {

  @Inject
  OpenFsConfig config;

  @ParameterizedTest
  @ValueSource(strings = {
    "ab",
    "UPPERCASE",
    "-startwithhyphen",
    "endwithhyphen-",
    ".startwithdot",
    "endwithdot.",
    "has space",
    "bucket%name",
    "bucket#1",
    "bucket@name",
    "bucket!test",
    "bucket$name",
    "bucket_name",
    "bucket+name"
  })
  void testCreateBucketWithInvalidName(String invalidBucketName) {
    String baseUrl = "http://localhost:" + config.s3().port();

    try (S3Client s3 = S3ClientFactory.create(baseUrl)) {
      assertThrows(IllegalArgumentException.class,
        () -> s3.createBucket(CreateBucketRequest.builder().bucket(invalidBucketName).build()));
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "..",
    "../etc",
    "../../etc",
    "foo/..",
    "foo/../bar",
    "./foo",
    "foo/.",
    "foo/./bar"
  })
  void testCreateBucketWithPathTraversalReturns404(String invalidBucketName) {
    String baseUrl = "http://localhost:" + config.s3().port();

    try (S3Client s3 = S3ClientFactory.create(baseUrl)) {
      assertThrows(IllegalArgumentException.class,
        () -> s3.createBucket(CreateBucketRequest.builder().bucket(invalidBucketName).build()));
    }
  }
}

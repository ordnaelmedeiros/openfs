package br.com.ordnaelmedeiros.openfs.s3.endpoints.createbucket;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import br.com.ordnaelmedeiros.openfs.config.OpenFsConfig;
import br.com.ordnaelmedeiros.openfs.testutils.BaseResourceTestS3;
import br.com.ordnaelmedeiros.openfs.testutils.S3ClientFactory;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

@QuarkusTest
class CreateBucketValidNameTest extends BaseResourceTestS3 {

  @Inject
  OpenFsConfig config;

  @ParameterizedTest
  @ValueSource(strings = {
    "bucket123",
    "123bucket",
    "bucket-123",
    "bucket.123",
    "my-bucket-name",
    "my.bucket.name",
    "a1b2c3",
    "test-bucket-2026"
  })
  void testCreateBucketWithValidName(String validBucketName) {
    String baseUrl = "http://localhost:" + config.s3().port();

    try (S3Client s3 = S3ClientFactory.create(baseUrl)) {
      assertDoesNotThrow(() -> s3.createBucket(CreateBucketRequest.builder().bucket(validBucketName).build()));
      assertDoesNotThrow(() -> s3.headBucket(HeadBucketRequest.builder().bucket(validBucketName).build()));
    }
  }
}

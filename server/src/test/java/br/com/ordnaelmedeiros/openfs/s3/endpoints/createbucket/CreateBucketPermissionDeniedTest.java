package br.com.ordnaelmedeiros.openfs.s3.endpoints.createbucket;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.ordnaelmedeiros.openfs.testutils.ReadOnlyDataPathResource;
import br.com.ordnaelmedeiros.openfs.testutils.S3ClientFactory;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@QuarkusTest
@QuarkusTestResource(value = ReadOnlyDataPathResource.class, restrictToAnnotatedClass = true)
class CreateBucketPermissionDeniedTest {

  @Test
  void testCreateBucketReturns500WhenPathIsReadOnly() {
    try (S3Client s3 = S3ClientFactory.create("http://localhost:9083")) {
      S3Exception exception = assertThrows(S3Exception.class,
        () -> s3.createBucket(CreateBucketRequest.builder().bucket("test-bucket").build()));

      assertTrue(exception.statusCode() == 500, "Expected status 500 but got " + exception.statusCode());
    }
  }
}

package br.com.ordnaelmedeiros.openfs.s3.endpoints.deletebucket;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.ordnaelmedeiros.openfs.config.OpenFsConfig;
import br.com.ordnaelmedeiros.openfs.testutils.BaseResourceTestS3;
import br.com.ordnaelmedeiros.openfs.testutils.OpenFsContainerExtension;
import br.com.ordnaelmedeiros.openfs.testutils.S3ClientFactory;
import br.com.ordnaelmedeiros.openfs.testutils.Target;
import br.com.ordnaelmedeiros.openfs.testutils.TargetProvider;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.nio.file.Files;
import java.nio.file.Path;

@QuarkusTest
@ExtendWith(OpenFsContainerExtension.class)
class DeleteBucketS3Test extends BaseResourceTestS3 {

  @Inject
  OpenFsConfig openFsConfig;

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(TargetProvider.class)
  void testDeleteBucket(Target target) {
    String bucketName = "delete-s3-client";

    try (S3Client s3 = S3ClientFactory.create(baseUrl(target))) {
      assertDoesNotThrow(() -> s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build()));
      assertDoesNotThrow(() -> s3.deleteBucket(DeleteBucketRequest.builder().bucket(bucketName).build()));

      S3Exception exception = org.junit.jupiter.api.Assertions.assertThrows(S3Exception.class,
        () -> s3.headBucket(HeadBucketRequest.builder().bucket(bucketName).build()));
      assertTrue(exception.statusCode() == 404, "Expected status 404 but got " + exception.statusCode());
    }

    if (target == Target.QUARKUS) {
      Path bucketPath = Path.of(openFsConfig.data().path()).resolve(bucketName);
      assertFalse(Files.exists(bucketPath), "Bucket directory should be deleted at " + bucketPath);
    }
  }
}

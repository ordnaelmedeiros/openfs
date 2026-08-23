package br.com.ordnaelmedeiros.openfs.s3.endpoints.createbucket;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

import java.nio.file.Files;
import java.nio.file.Path;

@QuarkusTest
@ExtendWith(OpenFsContainerExtension.class)
class CreateBucketS3Test extends BaseResourceTestS3 {

  @Inject
  OpenFsConfig openFsConfig;

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(TargetProvider.class)
  void testCreateBucket(Target target) {
    String bucketName = "test-s3-client";

    try (S3Client s3 = S3ClientFactory.create(baseUrl(target))) {
      assertDoesNotThrow(() -> s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build()));
      assertDoesNotThrow(() -> s3.headBucket(HeadBucketRequest.builder().bucket(bucketName).build()));
    }

    if (target == Target.QUARKUS) {
      Path bucketPath = Path.of(openFsConfig.data().path()).resolve(bucketName);
      assertTrue(Files.isDirectory(bucketPath), "Bucket directory should exist at " + bucketPath);
    }
  }
}

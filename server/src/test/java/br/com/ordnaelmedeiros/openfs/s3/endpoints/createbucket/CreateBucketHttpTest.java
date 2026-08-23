package br.com.ordnaelmedeiros.openfs.s3.endpoints.createbucket;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.ordnaelmedeiros.openfs.config.OpenFsConfig;
import br.com.ordnaelmedeiros.openfs.testutils.BaseResourceTestS3;
import br.com.ordnaelmedeiros.openfs.testutils.OpenFsContainerExtension;
import br.com.ordnaelmedeiros.openfs.testutils.Target;
import br.com.ordnaelmedeiros.openfs.testutils.TargetProvider;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.nio.file.Files;
import java.nio.file.Path;

@QuarkusTest
@ExtendWith(OpenFsContainerExtension.class)
class CreateBucketHttpTest extends BaseResourceTestS3 {

  @Inject
  OpenFsConfig openFsConfig;

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(TargetProvider.class)
  void testCreateBucket(Target target) {
    String bucketName = "test-http-client";

    given()
      .when()
        .put(baseUrl(target) + "/" + bucketName + "/")
      .then()
        .statusCode(200);

    given()
      .when()
        .head(baseUrl(target) + "/" + bucketName + "/")
      .then()
        .statusCode(200);

    if (target == Target.QUARKUS) {
      Path bucketPath = Path.of(openFsConfig.data().path()).resolve(bucketName);
      assertTrue(Files.isDirectory(bucketPath), "Bucket directory should exist at " + bucketPath);
    }
  }
}

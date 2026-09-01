package br.com.ordnaelmedeiros.openfs.s3.endpoints.deletebucket;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
class DeleteBucketHttpTest extends BaseResourceTestS3 {

  @Inject
  OpenFsConfig openFsConfig;

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(TargetProvider.class)
  void testDeleteBucket(Target target) {
    String bucketName = "delete-http-client";

    given()
      .when()
        .put(baseUrl(target) + "/" + bucketName + "/")
      .then()
        .statusCode(200);

    given()
      .when()
        .delete(baseUrl(target) + "/" + bucketName + "/")
      .then()
        .statusCode(204);

    given()
      .when()
        .head(baseUrl(target) + "/" + bucketName + "/")
      .then()
        .statusCode(404);

    given()
      .when()
        .delete(baseUrl(target) + "/" + "delete-http-client-missing" + "/")
      .then()
        .statusCode(404);

    if (target == Target.QUARKUS) {
      Path bucketPath = Path.of(openFsConfig.data().path()).resolve(bucketName);
      assertFalse(Files.exists(bucketPath), "Bucket directory should be deleted at " + bucketPath);
    }
  }
}

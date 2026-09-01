package br.com.ordnaelmedeiros.openfs.s3.endpoints.deletebucket;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.ordnaelmedeiros.openfs.config.OpenFsConfig;
import br.com.ordnaelmedeiros.openfs.testutils.OpenFsTestDataResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@QuarkusTest
@QuarkusTestResource(value = OpenFsTestDataResource.class, restrictToAnnotatedClass = true)
class DeleteBucketNotEmptyTest {

  @Inject
  OpenFsConfig config;

  @Test
  void testDeleteNonEmptyBucketReturns409() throws IOException {
    String bucketName = "delete-not-empty";
    String baseUrl = "http://localhost:" + config.s3().port();

    given()
      .when()
        .put(baseUrl + "/" + bucketName + "/")
      .then()
        .statusCode(200);

    Path bucketPath = Path.of(config.data().path()).resolve(bucketName);
    Files.writeString(bucketPath.resolve("object.txt"), "content");

    given()
      .when()
        .delete(baseUrl + "/" + bucketName + "/")
      .then()
        .statusCode(409);

    assertTrue(Files.isDirectory(bucketPath), "Bucket directory should still exist at " + bucketPath);
  }
}

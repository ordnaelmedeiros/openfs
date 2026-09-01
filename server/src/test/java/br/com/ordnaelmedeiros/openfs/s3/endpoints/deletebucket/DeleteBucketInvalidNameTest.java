package br.com.ordnaelmedeiros.openfs.s3.endpoints.deletebucket;

import static io.restassured.RestAssured.given;

import br.com.ordnaelmedeiros.openfs.config.OpenFsConfig;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@QuarkusTest
class DeleteBucketInvalidNameTest {

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
  void testDeleteBucketWithInvalidNameReturns400(String invalidBucketName) {
    String baseUrl = "http://localhost:" + config.s3().port();
    String encodedName = URLEncoder.encode(invalidBucketName, StandardCharsets.UTF_8);

    given()
      .when()
        .delete(baseUrl + "/" + encodedName + "/")
      .then()
        .statusCode(400);
  }
}

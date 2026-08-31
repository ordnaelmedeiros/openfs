package br.com.ordnaelmedeiros.openfs.s3.endpoints.listbuckets;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import br.com.ordnaelmedeiros.openfs.testutils.BaseResourceTestS3;
import br.com.ordnaelmedeiros.openfs.testutils.OpenFsContainerExtension;
import br.com.ordnaelmedeiros.openfs.testutils.Target;
import br.com.ordnaelmedeiros.openfs.testutils.TargetProvider;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.UUID;

@QuarkusTest
@ExtendWith(OpenFsContainerExtension.class)
class ListBucketsHttpTest extends BaseResourceTestS3 {

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(TargetProvider.class)
  void testListBucketsXml(Target target) {
    String bucketName = "list-buckets-http-" + UUID.randomUUID();

    given()
      .when()
        .put(baseUrl(target) + "/" + bucketName + "/")
      .then()
        .statusCode(200);

    given()
      .when()
        .get(baseUrl(target) + "/")
      .then()
        .statusCode(200)
        .contentType(ContentType.XML)
        .body(containsString("ListAllMyBucketsResult"))
        .body(containsString("<Owner>"))
        .body(containsString("<Buckets>"))
        .body(containsString("<Bucket>"))
        .body(containsString("<Name>" + bucketName + "</Name>"))
        .body(containsString("<CreationDate>"));
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(TargetProvider.class)
  void testListBucketsJson(Target target) {
    String bucketName = "list-buckets-json-" + UUID.randomUUID();

    given()
      .when()
        .put(baseUrl(target) + "/" + bucketName + "/")
      .then()
        .statusCode(200);

    given()
      .when()
        .header("Accept", "application/json")
        .get(baseUrl(target) + "/")
      .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body(containsString(bucketName));
  }
}

package br.com.ordnaelmedeiros.openfs.s3.endpoints.listobjects;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

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
class ListObjectsHttpTest extends BaseResourceTestS3 {

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(TargetProvider.class)
  void testListObjectsV2XmlOnEmptyBucket(Target target) {
    String bucketName = "list-objects-http-" + UUID.randomUUID();

    given()
      .when()
        .put(baseUrl(target) + "/" + bucketName + "/")
      .then()
        .statusCode(200);

    given()
      .when()
        .get(baseUrl(target) + "/" + bucketName + "?list-type=2")
      .then()
        .statusCode(200)
        .contentType(ContentType.XML)
        .body(containsString("ListBucketResult"))
        .body(containsString("<Name>" + bucketName + "</Name>"))
        .body(containsString("<MaxKeys>1000</MaxKeys>"))
        .body(containsString("<KeyCount>0</KeyCount>"))
        .body(containsString("<IsTruncated>false</IsTruncated>"))
        .body(not(containsString("<Contents>")))
        .body(not(containsString("<CommonPrefixes>")));
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(TargetProvider.class)
  void testListObjectsV1XmlOmitsKeyCount(Target target) {
    String bucketName = "list-objects-http-v1-" + UUID.randomUUID();

    given()
      .when()
        .put(baseUrl(target) + "/" + bucketName + "/")
      .then()
        .statusCode(200);

    given()
      .when()
        .get(baseUrl(target) + "/" + bucketName)
      .then()
        .statusCode(200)
        .contentType(ContentType.XML)
        .body(containsString("ListBucketResult"))
        .body(containsString("<Name>" + bucketName + "</Name>"))
        .body(containsString("<IsTruncated>false</IsTruncated>"))
        .body(not(containsString("<KeyCount>")));
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(TargetProvider.class)
  void testListObjectsJsonOnEmptyBucket(Target target) {
    String bucketName = "list-objects-json-" + UUID.randomUUID();

    given()
      .when()
        .put(baseUrl(target) + "/" + bucketName + "/")
      .then()
        .statusCode(200);

    given()
      .when()
        .header("Accept", "application/json")
        .get(baseUrl(target) + "/" + bucketName + "?list-type=2")
      .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body(containsString("\"name\":\"" + bucketName + "\""))
        .body(containsString("\"keyCount\":0"));
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(TargetProvider.class)
  void testListObjectsNoSuchBucketReturns404Xml(Target target) {
    String bucketName = "list-objects-missing-" + UUID.randomUUID();

    given()
      .when()
        .get(baseUrl(target) + "/" + bucketName + "?list-type=2")
      .then()
        .statusCode(404)
        .contentType(ContentType.XML)
        .body(containsString("<Error>"))
        .body(containsString("<Code>NoSuchBucket</Code>"))
        .body(containsString("<Resource>/" + bucketName + "</Resource>"));
  }
}

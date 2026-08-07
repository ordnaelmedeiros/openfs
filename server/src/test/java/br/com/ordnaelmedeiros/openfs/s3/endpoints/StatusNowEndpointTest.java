package br.com.ordnaelmedeiros.openfs.s3.endpoints;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

import br.com.ordnaelmedeiros.openfs.testutils.BaseResourceTestS3;
import br.com.ordnaelmedeiros.openfs.testutils.OpenFsContainerExtension;
import br.com.ordnaelmedeiros.openfs.testutils.Target;
import br.com.ordnaelmedeiros.openfs.testutils.TargetProvider;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

@QuarkusTest
@ExtendWith(OpenFsContainerExtension.class)
class StatusNowEndpointTest extends BaseResourceTestS3 {

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(TargetProvider.class)
  void testNowEndpoint(Target target) {
    given()
        .when()
        .get(baseUrl(target) + "/status/now")
        .then()
        .statusCode(200)
        .body("now", notNullValue());
  }
}

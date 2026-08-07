package br.com.ordnaelmedeiros.openfs.api.endpoints;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import br.com.ordnaelmedeiros.openfs.testutils.BaseResourceTestAPI;
import br.com.ordnaelmedeiros.openfs.testutils.OpenFsContainerExtension;
import br.com.ordnaelmedeiros.openfs.testutils.Target;
import br.com.ordnaelmedeiros.openfs.testutils.TargetProvider;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

@QuarkusTest
@ExtendWith(OpenFsContainerExtension.class)
class GreetingEndpointTest extends BaseResourceTestAPI {

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(TargetProvider.class)
  void testHelloEndpoint(Target target) {
    given()
        .when()
        .get(baseUrl(target) + "/hello")
        .then()
        .statusCode(200)
        .body(is("Hello World"));
  }
}

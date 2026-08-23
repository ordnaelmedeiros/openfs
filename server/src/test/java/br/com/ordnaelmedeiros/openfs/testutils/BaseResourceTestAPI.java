package br.com.ordnaelmedeiros.openfs.testutils;

import br.com.ordnaelmedeiros.openfs.config.OpenFsConfig;
import io.quarkus.test.common.QuarkusTestResource;
import jakarta.inject.Inject;

@QuarkusTestResource(value = OpenFsTestDataResource.class, restrictToAnnotatedClass = true)
public abstract class BaseResourceTestAPI {
  protected static final int HTTP_CONTAINER_PORT = 8082;

  @Inject
  OpenFsConfig config;

  protected String baseUrl(Target target) {
    return switch (target) {
      case QUARKUS -> "http://localhost:" + config.http().port();
      case JVM_CONTAINER -> OpenFsContainers.jvmUrl(HTTP_CONTAINER_PORT);
      case NATIVE_CONTAINER -> OpenFsContainers.nativeUrl(HTTP_CONTAINER_PORT);
    };
  }
}

package br.com.ordnaelmedeiros.openfs.testutils;

import br.com.ordnaelmedeiros.openfs.config.OpenFsConfig;
import io.quarkus.test.common.QuarkusTestResource;
import jakarta.inject.Inject;

@QuarkusTestResource(value = OpenFsTestDataResource.class, restrictToAnnotatedClass = true)
public abstract class BaseResourceTestS3 {
  protected static final int S3_CONTAINER_PORT = 8083;

  @Inject
  OpenFsConfig config;

  protected String baseUrl(Target target) {
    return switch (target) {
      case QUARKUS -> "http://localhost:" + config.s3().port();
      case JVM_CONTAINER -> OpenFsContainers.jvmUrl(S3_CONTAINER_PORT);
      case NATIVE_CONTAINER -> OpenFsContainers.nativeUrl(S3_CONTAINER_PORT);
    };
  }
}

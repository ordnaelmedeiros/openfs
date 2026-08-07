package br.com.ordnaelmedeiros.openfs.testutils;

import br.com.ordnaelmedeiros.openfs.config.OpenFsConfig;
import jakarta.inject.Inject;

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

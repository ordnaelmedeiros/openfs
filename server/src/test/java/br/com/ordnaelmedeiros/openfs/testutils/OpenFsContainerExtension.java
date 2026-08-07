package br.com.ordnaelmedeiros.openfs.testutils;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class OpenFsContainerExtension implements BeforeAllCallback {
  @Override
  public void beforeAll(ExtensionContext context) {
    if (TestConfig.isContainersEnabled()) {
      OpenFsContainers.start();
    }
  }
}

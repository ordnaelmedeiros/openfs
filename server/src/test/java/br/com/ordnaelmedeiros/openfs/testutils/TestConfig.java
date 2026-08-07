package br.com.ordnaelmedeiros.openfs.testutils;

public final class TestConfig {
  private TestConfig() {}

  public static boolean isContainersEnabled() {
    return Boolean.getBoolean("test.containers.enabled");
  }
}

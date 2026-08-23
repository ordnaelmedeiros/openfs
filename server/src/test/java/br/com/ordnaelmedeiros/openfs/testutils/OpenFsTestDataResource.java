package br.com.ordnaelmedeiros.openfs.testutils;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;

public class OpenFsTestDataResource implements QuarkusTestResourceLifecycleManager {

  private Path dataDir;

  @Override
  public Map<String, String> start() {
    try {
      dataDir = Files.createTempDirectory("openfs-test-" + UUID.randomUUID());
      return Map.of("openfs.data.path", dataDir.toAbsolutePath().toString());
    } catch (IOException e) {
      throw new RuntimeException("Failed to create test data directory", e);
    }
  }

  @Override
  public void stop() {
    if (dataDir != null && Files.exists(dataDir)) {
      try (var stream = Files.walk(dataDir)) {
        stream.sorted(Comparator.reverseOrder())
          .forEach(path -> {
            try {
              Files.deleteIfExists(path);
            } catch (IOException ignored) {
            }
          });
      } catch (IOException ignored) {
      }
    }
  }
}

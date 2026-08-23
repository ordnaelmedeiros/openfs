package br.com.ordnaelmedeiros.openfs.testutils;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Map;
import java.util.Set;

public class ReadOnlyDataPathResource implements QuarkusTestResourceLifecycleManager {

  private Path readOnlyDir;

  @Override
  public Map<String, String> start() {
    try {
      readOnlyDir = Files.createTempDirectory("openfs-readonly-");
      Files.setPosixFilePermissions(readOnlyDir, Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_EXECUTE));
      return Map.of("openfs.data.path", readOnlyDir.toAbsolutePath().toString());
    } catch (IOException e) {
      throw new RuntimeException("Failed to create read-only test directory", e);
    }
  }

  @Override
  public void stop() {
    if (readOnlyDir != null && Files.exists(readOnlyDir)) {
      try {
        Files.setPosixFilePermissions(readOnlyDir, Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE));
        Files.deleteIfExists(readOnlyDir);
      } catch (IOException ignored) {
      }
    }
  }
}

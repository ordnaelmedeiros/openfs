package br.com.ordnaelmedeiros.openfs.testutils;

import java.nio.file.Paths;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

public final class OpenFsContainers {
  private static final GenericContainer<?> NATIVE = new GenericContainer<>(new ImageFromDockerfile().withDockerfile(Paths.get("../Dockerfile.native"))).withExposedPorts(8082, 8083);
  private static final GenericContainer<?> JVM = new GenericContainer<>(new ImageFromDockerfile().withDockerfile(Paths.get("../Dockerfile.jvm"))).withExposedPorts(8082, 8083);
  private static boolean started;

  private OpenFsContainers() {
  }

  static synchronized void start() {
    if (!started) {
      JVM.start();
      NATIVE.start();
      started = true;
    }
  }

  public static String nativeUrl(int port) {
    return url(NATIVE, port);
  }

  public static String jvmUrl(int port) {
    return url(JVM, port);
  }

  private static String url(GenericContainer<?> app, int port) {
    return "http://" + app.getHost() + ":" + app.getMappedPort(port);
  }
}

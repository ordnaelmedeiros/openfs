package br.com.ordnaelmedeiros.openfs.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "openfs")
public interface OpenFsConfig {
  PortConfig management();

  PortConfig http();

  PortConfig s3();

  LogConfig log();

  interface PortConfig {
    int port();

    int testPort();

    @io.smallrye.config.WithDefault("10")
    int virtualThreadsPoolSize();
  }

  interface LogConfig {
    boolean access();
  }
}

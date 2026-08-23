package br.com.ordnaelmedeiros.openfs.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "openfs")
public interface OpenFsConfig {
  PortConfig management();

  PortConfig http();

  S3Config s3();

  DataConfig data();

  LogConfig log();

  interface DataConfig {
    String path();
  }

  interface PortConfig {
    int port();
  }

  interface S3Config {
    int port();

    int virtualThreadsPoolSize();
  }

  interface LogConfig {
    boolean access();
  }
}

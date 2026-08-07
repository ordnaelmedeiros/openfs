package br.com.ordnaelmedeiros.openfs.s3.server;

import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.mutiny.ext.web.RoutingContext;

public interface S3Endpoint {

  record Request(HttpMethod method, String path) {}

  Request request();
  Uni<Void> handle(RoutingContext ctx);
}

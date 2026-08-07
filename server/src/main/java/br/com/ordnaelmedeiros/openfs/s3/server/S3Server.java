package br.com.ordnaelmedeiros.openfs.s3.server;

import br.com.ordnaelmedeiros.openfs.config.OpenFsConfig;
import io.quarkus.runtime.StartupEvent;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.Router;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@ApplicationScoped
public class S3Server {

  @Inject
  OpenFsConfig config;

  @Inject
  Instance<S3Endpoint> endpoints;

  @Inject
  RequestLogHandler requestLogHandler;

  private ExecutorService virtualThreadExecutor;

  void start(@Observes StartupEvent event, Vertx vertx) {
    int poolSize = config.s3().virtualThreadsPoolSize();
    ThreadFactory threadFactory = Thread.ofVirtual().name("s3-virtual-thread-", 0).factory();
    virtualThreadExecutor = Executors.newFixedThreadPool(poolSize, threadFactory);

    Router router = Router.router(vertx);

    if (config.log().access()) {
      router.route().handler(requestLogHandler);
    }

    for (S3Endpoint endpoint : endpoints) {
      S3Endpoint.Request request = endpoint.request();
      router.route(request.method(), request.path()).handler(ctx -> {
        virtualThreadExecutor.execute(() -> {
          endpoint.handle(ctx)
            .subscribe().with(
              v -> {},
              err -> ctx.fail(err)
            );
        });
      });
    }

    vertx.createHttpServer()
      .requestHandler(router)
      .listenAndAwait(config.s3().port());
  }
}

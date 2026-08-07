package br.com.ordnaelmedeiros.openfs.s3.server;

import io.vertx.mutiny.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.function.Consumer;

@ApplicationScoped
public class RequestLogHandler implements Consumer<RoutingContext> {

  private static final Logger LOG = Logger.getLogger("openfs.s3.access-log");

  @Override
  public void accept(RoutingContext ctx) {
    long start = System.nanoTime();
    ctx.response().endHandler(() -> {
      long durationMs = (System.nanoTime() - start) / 1_000_000;
      LOG.infof("%s %s %d %dms",
          ctx.request().method(),
          ctx.request().path(),
          ctx.response().getStatusCode(),
          durationMs);
    });
    ctx.next();
  }
}

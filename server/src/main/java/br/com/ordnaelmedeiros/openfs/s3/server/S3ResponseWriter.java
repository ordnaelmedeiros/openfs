package br.com.ordnaelmedeiros.openfs.s3.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpHeaders;
import io.vertx.mutiny.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;

@ApplicationScoped
public class S3ResponseWriter {

  private static final XmlMapper XML_MAPPER = XmlMapper.builder().build();

  @Inject
  ObjectMapper jsonMapper;

  public Uni<Void> write(RoutingContext ctx, Object entity) {
    return Uni.createFrom().voidItem().onItem().invoke(() -> {
      ctx.response().setStatusCode(200);

      if (entity == null) {
        ctx.response().endAndForget();
        return;
      }

      if (entity instanceof String str) {
        ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
        ctx.response().endAndForget(str);
        return;
      }

      String accept = ctx.request().getHeader(HttpHeaders.ACCEPT);
      try {
        if (accept != null && accept.contains(MediaType.APPLICATION_JSON)) {
          String body = jsonMapper.writeValueAsString(entity);
          ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
          ctx.response().endAndForget(body);
        } else {
          String body = XML_MAPPER.writeValueAsString(entity);
          ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML);
          ctx.response().endAndForget(body);
        }
      } catch (Exception e) {
        ctx.response().setStatusCode(500).endAndForget("Error: " + e.getMessage());
      }
    });
  }
}

package br.com.ordnaelmedeiros.openfs.domain.status;

import br.com.ordnaelmedeiros.openfs.api.endpoints.StatusEndpoint;
import io.smallrye.common.annotation.RunOnVirtualThread;
import java.time.LocalDateTime;

@RunOnVirtualThread
public class StatusResource implements StatusEndpoint {

  @Override
  public StatusResponse now() {
    return new StatusResponse(LocalDateTime.now().toString());
  }
}

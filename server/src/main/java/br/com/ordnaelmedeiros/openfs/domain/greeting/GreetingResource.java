package br.com.ordnaelmedeiros.openfs.domain.greeting;

import br.com.ordnaelmedeiros.openfs.api.endpoints.HelloEndpoint;
import io.smallrye.common.annotation.RunOnVirtualThread;

@RunOnVirtualThread
public class GreetingResource implements HelloEndpoint {

  @Override
  public String hello() {
    return "Hello World";
  }
}

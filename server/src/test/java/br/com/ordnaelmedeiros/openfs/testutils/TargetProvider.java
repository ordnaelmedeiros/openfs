package br.com.ordnaelmedeiros.openfs.testutils;

import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class TargetProvider implements ArgumentsProvider {

  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
    if (TestConfig.isContainersEnabled()) {
      return Stream.of(
          Arguments.of(Target.QUARKUS),
          Arguments.of(Target.JVM_CONTAINER),
          Arguments.of(Target.NATIVE_CONTAINER));
    } else {
      return Stream.of(Arguments.of(Target.QUARKUS));
    }
  }
}

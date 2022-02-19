/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.micrometer;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.returns;

import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import java.util.ArrayList;
import java.util.List;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public final class ActuatorInstrumentation implements TypeInstrumentation {

  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return named("org.springframework.boot.autoconfigure.AutoConfigurationImportSelector");
  }

  @Override
  public void transform(TypeTransformer transformer) {
    transformer.applyAdviceToMethod(
        named("getCandidateConfigurations").and(returns(List.class)),
        ActuatorInstrumentation.class.getName() + "$GetCandidateConfigurationsAdvice");
  }

  public static class GetCandidateConfigurationsAdvice {

    @Advice.OnMethodExit(suppress = Throwable.class)
    public static void onExit(@Advice.Return(readOnly = false) List<String> configurations) {
      if (configurations.contains(
          "org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration")) {
        List<String> configs = new ArrayList<>(configurations.size() + 1);
        configs.addAll(configurations);
        // using class reference here so that muzzle will consider it a dependency of this advice
        configs.add(AzureMonitorAutoConfiguration.class.getName());
        configs.remove(
            "com.microsoft.azure.spring.autoconfigure.metrics.AzureMonitorMetricsExportAutoConfiguration");
        configurations = configs;
      }
    }
  }
}
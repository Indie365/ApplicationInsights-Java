/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.applicationinsightsweb;

import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

import com.microsoft.applicationinsights.web.internal.RequestTelemetryContext;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.api.tracer.ServerSpan;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import io.opentelemetry.javaagent.instrumentation.api.InstrumentationContext;
import io.opentelemetry.javaagent.instrumentation.api.Java8BytecodeBridge;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class ThreadContextInstrumentation implements TypeInstrumentation {
  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return named("com.microsoft.applicationinsights.web.internal.ThreadContext");
  }

  @Override
  public void transform(TypeTransformer transformer) {
    transformer.applyAdviceToMethod(
        isMethod().and(isPublic()).and(named("getRequestTelemetryContext")).and(takesArguments(0)),
        ThreadContextInstrumentation.class.getName() + "$GetRequestTelemetryContextAdvice");
  }

  public static class GetRequestTelemetryContextAdvice {
    @Advice.OnMethodExit
    public static void methodExit(
        @Advice.Return(readOnly = false) RequestTelemetryContext requestTelemetryContext) {
      if (requestTelemetryContext != null) {
        // don't want to break code that was manually setting and retrieving this value
        return;
      }
      requestTelemetryContext = new RequestTelemetryContext(0);
      Span serverSpan = ServerSpan.fromContextOrNull(Java8BytecodeBridge.currentContext());
      InstrumentationContext.get(RequestTelemetryContext.class, Span.class)
          .put(requestTelemetryContext, serverSpan);
    }
  }
}
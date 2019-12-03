package datadog.opentracing


import datadog.trace.common.writer.ListWriter

class SpanFactory {

  static DDSpan newSpanOf(long timestampMicro, String threadName = Thread.currentThread().name) {
    def writer = new ListWriter()
    def tracer = new DDTracer(writer)
    def currentThreadName = Thread.currentThread().getName()
    Thread.currentThread().setName(threadName)
    def context = new DDSpanContext(
      1G,
      1G,
      0G,
      "fakeOperation",
      "fakeResource",
      false,
      "fakeType",
      Collections.emptyMap(),
      new PendingTrace(tracer, 1G),
      tracer)
    Thread.currentThread().setName(currentThreadName)
    return new DDSpan(timestampMicro, context)
  }

  static DDSpan newSpanOf(DDTracer tracer) {
    def context = new DDSpanContext(
      1G,
      1G,
      0G,
      "fakeOperation",
      "fakeResource",
      false,
      "fakeType",
      Collections.emptyMap(),
      new PendingTrace(tracer, 1G),
      tracer)
    return new DDSpan(1, context)
  }

  static DDSpan newSpanOf(PendingTrace trace) {
    def context = new DDSpanContext(
      trace.traceId,
      1G,
      0G,
      "fakeOperation",
      "fakeResource",
      false,
      "fakeType",
      Collections.emptyMap(),
      trace,
      trace.tracer)
    return new DDSpan(1, context)
  }

  static DDSpan newSpanOf(String envName) {
    def writer = new ListWriter()
    def tracer = new DDTracer(writer)
    def context = new DDSpanContext(
      1G,
      1G,
      0G,
      "fakeOperation",
      "fakeResource",
      false,
      "fakeType",
      Collections.emptyMap(),
      new PendingTrace(tracer, 1G),
      tracer)
    context.setTag("env", envName)
    return new DDSpan(0l, context)
  }
}
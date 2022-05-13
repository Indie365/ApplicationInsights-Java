plugins {
  id("groovy")

  id("ai.java-conventions")
  id("ai.javaagent-instrumentation")

  id("io.opentelemetry.instrumentation.muzzle-generation") version "1.13.1-alpha"
  id("io.opentelemetry.instrumentation.muzzle-check") version "1.13.1-alpha"
}

muzzle {
  pass {
    group.set("com.microsoft.azure")
    module.set("applicationinsights-web")
    versions.set("[2.3.0,)")
  }
}

val otelInstrumentationVersionAlpha: String by project
val otelVersion: String by project

dependencies {
  compileOnly("com.microsoft.azure:applicationinsights-web:2.3.0")

  testImplementation("io.opentelemetry:opentelemetry-extension-annotations:$otelVersion")

  testImplementation("com.microsoft.azure:applicationinsights-web:2.3.0")
  testImplementation("javax.servlet:javax.servlet-api:3.0.1")

  // TODO remove when start using io.opentelemetry.instrumentation.javaagent-instrumentation plugin
  add("codegen", "io.opentelemetry.javaagent:opentelemetry-javaagent-tooling:$otelInstrumentationVersionAlpha")
  add("codegen", "ch.qos.logback:logback-classic:1.2.3")
  add("muzzleBootstrap", "io.opentelemetry.instrumentation:opentelemetry-instrumentation-api-annotation-support:$otelInstrumentationVersionAlpha")
  add("muzzleTooling", "io.opentelemetry.javaagent:opentelemetry-javaagent-extension-api:$otelInstrumentationVersionAlpha")
  add("muzzleTooling", "io.opentelemetry.javaagent:opentelemetry-javaagent-tooling:$otelInstrumentationVersionAlpha")
}
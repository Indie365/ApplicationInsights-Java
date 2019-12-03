package datadog.trace.agent.decorator

import datadog.trace.api.DDTags
import datadog.trace.instrumentation.api.AgentSpan
import io.opentracing.tag.Tags

class ClientDecoratorTest extends BaseDecoratorTest {

  def span = Mock(AgentSpan)

  def "test afterStart"() {
    setup:
    def decorator = newDecorator((String) serviceName)

    when:
    decorator.afterStart(span)

    then:
    if (serviceName != null) {
      1 * span.setTag(DDTags.SERVICE_NAME, serviceName)
    }
    1 * span.setTag(Tags.COMPONENT.key, "test-component")
    1 * span.setTag(Tags.SPAN_KIND.key, "client")
    1 * span.setTag(DDTags.SPAN_TYPE, decorator.spanType())
    _ * span.setTag(_, _) // Want to allow other calls from child implementations.
    0 * _

    where:
    serviceName << ["test-service", "other-service", null]
  }

  def "test beforeFinish"() {
    when:
    newDecorator("test-service").beforeFinish(span)

    then:
    0 * _
  }

  @Override
  def newDecorator() {
    return newDecorator("test-service")
  }

  def newDecorator(String serviceName) {
    return new ClientDecorator() {
      @Override
      protected String[] instrumentationNames() {
        return ["test1", "test2"]
      }

      @Override
      protected String service() {
        return serviceName
      }

      @Override
      protected String spanType() {
        return "test-type"
      }

      @Override
      protected String component() {
        return "test-component"
      }
    }
  }
}
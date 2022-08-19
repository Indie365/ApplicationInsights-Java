/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.microsoft.applicationinsights.agent.internal.sampling;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.applicationinsights.agent.internal.configuration.Configuration;
import com.microsoft.applicationinsights.agent.internal.configuration.Configuration.MatchType;
import com.microsoft.applicationinsights.agent.internal.configuration.Configuration.SamplingOverride;
import com.microsoft.applicationinsights.agent.internal.configuration.Configuration.SamplingOverrideAttribute;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class SamplingOverridesTest {

  @Test
  void shouldSampleByDefault() {
    // given
    List<SamplingOverride> overrides = new ArrayList<>();
    SamplingOverrides samplingOverrides = new SamplingOverrides(overrides);
    Attributes attributes = Attributes.empty();

    // when
    Sampler sampler = samplingOverrides.getOverride(SpanKind.SERVER, attributes);

    // expect
    assertThat(sampler).isNull();
  }

  @Test
  void shouldFilterBySpanKind() {
    // given
    List<SamplingOverride> overrides =
        singletonList(newOverride(Configuration.SpanKind.SERVER, 25));
    SamplingOverrides samplingOverrides = new SamplingOverrides(overrides);
    Attributes attributes = Attributes.empty();

    // when
    Sampler sampler = samplingOverrides.getOverride(SpanKind.SERVER, attributes);

    // expect
    assertThat(sampler).isNotNull();
    assertThat(sampler.getDescription()).isEqualTo("AzureMonitorSampler{25.000%}");
  }

  @Test
  void shouldNotFilterBySpanKind() {
    // given
    List<SamplingOverride> overrides =
        singletonList(newOverride(Configuration.SpanKind.SERVER, 25));
    SamplingOverrides samplingOverrides = new SamplingOverrides(overrides);
    Attributes attributes = Attributes.empty();

    // when
    Sampler sampler = samplingOverrides.getOverride(SpanKind.CLIENT, attributes);

    // expect
    assertThat(sampler).isNull();
  }

  @Test
  void shouldFilterStrictMatch() {
    // given
    List<SamplingOverride> overrides =
        singletonList(
            newOverride(Configuration.SpanKind.SERVER, 25, newStrictAttribute("one", "1")));
    SamplingOverrides samplingOverrides = new SamplingOverrides(overrides);
    Attributes attributes = Attributes.of(AttributeKey.stringKey("one"), "1");

    // when
    Sampler sampler = samplingOverrides.getOverride(SpanKind.SERVER, attributes);

    // expect
    assertThat(sampler).isNotNull();
    assertThat(sampler.getDescription()).isEqualTo("AzureMonitorSampler{25.000%}");
  }

  @Test
  void shouldFilterStrictMatchWithNullSpanKind() {
    // given
    List<SamplingOverride> overrides =
        singletonList(newOverride(null, 25, newStrictAttribute("one", "1")));
    SamplingOverrides samplingOverrides = new SamplingOverrides(overrides);
    Attributes attributes = Attributes.of(AttributeKey.stringKey("one"), "1");

    // when
    Sampler sampler = samplingOverrides.getOverride(SpanKind.SERVER, attributes);

    // expect
    assertThat(sampler).isNotNull();
    assertThat(sampler.getDescription()).isEqualTo("AzureMonitorSampler{25.000%}");
  }

  @Test
  void shouldNotFilterStrictMatchWithWrongSpanKind() {
    // given
    List<SamplingOverride> overrides =
        singletonList(
            newOverride(Configuration.SpanKind.SERVER, 25, newStrictAttribute("one", "1")));
    SamplingOverrides samplingOverrides = new SamplingOverrides(overrides);
    Attributes attributes = Attributes.of(AttributeKey.stringKey("one"), "1");

    // when
    Sampler sampler = samplingOverrides.getOverride(SpanKind.CLIENT, attributes);

    // expect
    assertThat(sampler).isNull();
  }

  @Test
  void shouldNotFilterStrictMatch() {
    // given
    List<SamplingOverride> overrides =
        singletonList(
            newOverride(Configuration.SpanKind.SERVER, 25, newStrictAttribute("one", "1")));
    SamplingOverrides samplingOverrides = new SamplingOverrides(overrides);
    Attributes attributes = Attributes.of(AttributeKey.stringKey("one"), "2");

    // when
    Sampler sampler = samplingOverrides.getOverride(SpanKind.SERVER, attributes);

    // expect
    assertThat(sampler).isNull();
  }

  @Test
  void shouldNotFilterMissingStrictMatch() {
    // given
    List<SamplingOverride> overrides =
        singletonList(
            newOverride(Configuration.SpanKind.SERVER, 25, newStrictAttribute("one", "1")));
    SamplingOverrides samplingOverrides = new SamplingOverrides(overrides);
    Attributes attributes = Attributes.of(AttributeKey.stringKey("two"), "1");

    // when
    Sampler sampler = samplingOverrides.getOverride(SpanKind.SERVER, attributes);

    // expect
    assertThat(sampler).isNull();
  }

  @Test
  void shouldFilterRegexpMatch() {
    // given
    List<SamplingOverride> overrides =
        singletonList(
            newOverride(Configuration.SpanKind.SERVER, 25, newRegexpAttribute("one", "1.*")));
    SamplingOverrides samplingOverrides = new SamplingOverrides(overrides);
    Attributes attributes = Attributes.of(AttributeKey.stringKey("one"), "11");

    // when
    Sampler sampler = samplingOverrides.getOverride(SpanKind.SERVER, attributes);

    // expect
    assertThat(sampler).isNotNull();
    assertThat(sampler.getDescription()).isEqualTo("AzureMonitorSampler{25.000%}");
  }

  @Test
  void shouldNotFilterRegexpMatch() {
    // given
    List<SamplingOverride> overrides =
        singletonList(
            newOverride(Configuration.SpanKind.SERVER, 25, newRegexpAttribute("one", "1.*")));
    SamplingOverrides samplingOverrides = new SamplingOverrides(overrides);
    Attributes attributes = Attributes.of(AttributeKey.stringKey("one"), "22");

    // when
    Sampler sampler = samplingOverrides.getOverride(SpanKind.SERVER, attributes);

    // expect
    assertThat(sampler).isNull();
  }

  @Test
  void shouldNotFilterMissingRegexpMatch() {
    // given
    List<SamplingOverride> overrides =
        singletonList(
            newOverride(Configuration.SpanKind.SERVER, 25, newRegexpAttribute("one", "1.*")));
    SamplingOverrides samplingOverrides = new SamplingOverrides(overrides);
    Attributes attributes = Attributes.of(AttributeKey.stringKey("two"), "11");

    // when
    Sampler sampler = samplingOverrides.getOverride(SpanKind.SERVER, attributes);

    // expect
    assertThat(sampler).isNull();
  }

  @Test
  void shouldFilterKeyOnlyMatch() {
    // given
    List<SamplingOverride> overrides =
        singletonList(newOverride(Configuration.SpanKind.SERVER, 25, newKeyOnlyAttribute("one")));
    SamplingOverrides samplingOverrides = new SamplingOverrides(overrides);
    Attributes attributes = Attributes.of(AttributeKey.stringKey("one"), "11");

    // when
    Sampler sampler = samplingOverrides.getOverride(SpanKind.SERVER, attributes);

    // expect
    assertThat(sampler).isNotNull();
    assertThat(sampler.getDescription()).isEqualTo("AzureMonitorSampler{25.000%}");
  }

  @Test
  void shouldNotFilterKeyOnlyMatch() {
    // given
    List<SamplingOverride> overrides =
        singletonList(newOverride(Configuration.SpanKind.SERVER, 25, newKeyOnlyAttribute("one")));
    SamplingOverrides samplingOverrides = new SamplingOverrides(overrides);
    Attributes attributes = Attributes.of(AttributeKey.stringKey("two"), "22");

    // when
    Sampler sampler = samplingOverrides.getOverride(SpanKind.SERVER, attributes);

    // expect
    assertThat(sampler).isNull();
  }

  @Test
  void shouldFilterMultiAttributes() {
    // given
    List<SamplingOverride> overrides =
        singletonList(
            newOverride(
                Configuration.SpanKind.SERVER,
                25,
                newStrictAttribute("one", "1"),
                newRegexpAttribute("two", "2.*")));
    SamplingOverrides samplerOverride = new SamplingOverrides(overrides);
    Attributes attributes =
        Attributes.of(AttributeKey.stringKey("one"), "1", AttributeKey.stringKey("two"), "22");

    // when
    Sampler sampler = samplerOverride.getOverride(SpanKind.SERVER, attributes);

    // expect
    assertThat(sampler).isNotNull();
    assertThat(sampler.getDescription()).isEqualTo("AzureMonitorSampler{25.000%}");
  }

  @Test
  void shouldNotFilterMultiAttributes() {
    // given
    List<SamplingOverride> overrides =
        singletonList(
            newOverride(
                Configuration.SpanKind.SERVER,
                25,
                newStrictAttribute("one", "1"),
                newRegexpAttribute("two", "2.*")));
    SamplingOverrides samplingOverrides = new SamplingOverrides(overrides);
    Attributes attributes =
        Attributes.of(AttributeKey.stringKey("one"), "2", AttributeKey.stringKey("two"), "22");

    // when
    Sampler sampler = samplingOverrides.getOverride(SpanKind.SERVER, attributes);

    // expect
    assertThat(sampler).isNull();
  }

  @Test
  void shouldFilterMultiConfigsBothMatch() {
    // given
    List<SamplingOverride> overrides =
        Arrays.asList(
            newOverride(Configuration.SpanKind.SERVER, 25, newStrictAttribute("one", "1")),
            newOverride(Configuration.SpanKind.SERVER, 0, newRegexpAttribute("two", "2.*")));
    SamplingOverrides samplingOverrides = new SamplingOverrides(overrides);
    Attributes attributes =
        Attributes.of(AttributeKey.stringKey("one"), "1", AttributeKey.stringKey("two"), "22");

    // when
    Sampler sampler = samplingOverrides.getOverride(SpanKind.SERVER, attributes);

    // expect
    assertThat(sampler).isNotNull();
    assertThat(sampler.getDescription()).isEqualTo("AzureMonitorSampler{25.000%}");
  }

  @Test
  void shouldFilterMultiConfigsOneMatch() {
    // given
    List<SamplingOverride> overrides =
        Arrays.asList(
            newOverride(Configuration.SpanKind.SERVER, 50, newStrictAttribute("one", "1")),
            newOverride(Configuration.SpanKind.SERVER, 25, newRegexpAttribute("two", "2.*")));
    SamplingOverrides samplingOverrides = new SamplingOverrides(overrides);
    Attributes attributes =
        Attributes.of(AttributeKey.stringKey("one"), "2", AttributeKey.stringKey("two"), "22");

    // when
    Sampler sampler = samplingOverrides.getOverride(SpanKind.SERVER, attributes);

    // expect
    assertThat(sampler).isNotNull();
    assertThat(sampler.getDescription()).isEqualTo("AzureMonitorSampler{25.000%}");
  }

  @Test
  void shouldNotFilterMultiConfigsNoMatch() {
    // given
    List<SamplingOverride> overrides =
        Arrays.asList(
            newOverride(Configuration.SpanKind.SERVER, 50, newStrictAttribute("one", "1")),
            newOverride(Configuration.SpanKind.SERVER, 25, newRegexpAttribute("two", "2.*")));
    SamplingOverrides samplingOverrides = new SamplingOverrides(overrides);
    Attributes attributes =
        Attributes.of(AttributeKey.stringKey("one"), "2", AttributeKey.stringKey("two"), "33");

    // when
    Sampler sampler = samplingOverrides.getOverride(SpanKind.SERVER, attributes);

    // expect
    assertThat(sampler).isNull();
  }

  private static SamplingOverride newOverride(
      Configuration.SpanKind spanKind, float percentage, SamplingOverrideAttribute... attribute) {
    SamplingOverride override = new SamplingOverride();
    override.spanKind = spanKind;
    override.attributes = Arrays.asList(attribute);
    override.percentage = percentage;
    return override;
  }

  private static SamplingOverrideAttribute newStrictAttribute(String key, String value) {
    SamplingOverrideAttribute attribute = new SamplingOverrideAttribute();
    attribute.key = key;
    attribute.value = value;
    attribute.matchType = MatchType.STRICT;
    return attribute;
  }

  private static SamplingOverrideAttribute newRegexpAttribute(String key, String value) {
    SamplingOverrideAttribute attribute = new SamplingOverrideAttribute();
    attribute.key = key;
    attribute.value = value;
    attribute.matchType = MatchType.REGEXP;
    return attribute;
  }

  private static SamplingOverrideAttribute newKeyOnlyAttribute(String key) {
    SamplingOverrideAttribute attribute = new SamplingOverrideAttribute();
    attribute.key = key;
    return attribute;
  }
}

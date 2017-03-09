/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.github.yuzutech.bear;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.util.Collections;

import org.junit.Test;

public class GrokProcessorTest {

  @Test
  public void testMatch() throws Exception {
    String fieldName = "test";
    Event event = new Event();
    event.setFieldValue(fieldName, "1");
    GrokProcessor processor = new GrokProcessor(Collections.singletonMap("ONE", "1"),
        Collections.singletonList("%{ONE:one}"), fieldName, false, false);
    processor.execute(event);
    assertThat(event.getFieldValue("one", String.class)).isEqualTo("1");
  }

  @Test
  public void testNoMatch() {
    String fieldName = "test";
    Event event = new Event();
    event.setFieldValue(fieldName, "23");
    GrokProcessor processor = new GrokProcessor(Collections.singletonMap("ONE", "1"),
        Collections.singletonList("%{ONE:one}"), fieldName, false, false);
    assertThatThrownBy(() -> processor.execute(event))
        .hasMessage("Provided Grok expressions do not match field value: [23]");
  }
}
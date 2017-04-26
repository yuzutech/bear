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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GrokProcessor {

  private static final String PATTERN_MATCH_KEY = "_bear._grok_match_index";

  private final String matchField;
  private final List<String> matchPatterns;
  private final Grok grok;
  private final boolean traceMatch;
  private final boolean ignoreMissing;

  public GrokProcessor(Map<String, String> patternBank, List<String> matchPatterns, String matchField, boolean traceMatch, boolean ignoreMissing) {
    this.matchField = matchField;
    this.matchPatterns = matchPatterns;
    this.grok = new Grok(patternBank, combinePatterns(matchPatterns, traceMatch));
    this.traceMatch = traceMatch;
    this.ignoreMissing = ignoreMissing;
  }

  public void execute(Event event) {
    String fieldValue = event.get(matchField, String.class);

    if (fieldValue == null && ignoreMissing) {
      return;
    } else if (fieldValue == null) {
      throw new IllegalArgumentException("field [" + matchField + "] is null, cannot process it.");
    }

    Map<String, Object> matches = grok.captures(fieldValue);
    if (matches == null) {
      throw new IllegalArgumentException("Provided Grok expressions do not match field value: [" + fieldValue + "]");
    }

    matches.forEach(event::set);

    if (traceMatch) {
      if (matchPatterns.size() > 1) {
        @SuppressWarnings("unchecked")
        HashMap<String, String> matchMap = (HashMap<String, String>) event.get(PATTERN_MATCH_KEY, Object.class);
        matchMap.keySet().stream().findFirst().ifPresent((index) -> {
          event.set(PATTERN_MATCH_KEY, index);
        });
      } else {
        event.set(PATTERN_MATCH_KEY, "0");
      }
    }
  }

  private static String combinePatterns(List<String> patterns, boolean traceMatch) {
    StringBuilder combinedPattern;
    if (patterns.size() > 1) {
      combinedPattern = new StringBuilder();
      for (int i = 0; i < patterns.size(); i++) {
        String pattern = patterns.get(i);
        String valueWrap;
        if (traceMatch) {
          valueWrap = "(?<" + PATTERN_MATCH_KEY + "." + i + ">" + pattern + ")";
        } else {
          valueWrap = "(?:" + patterns.get(i) + ")";
        }
        if (combinedPattern.toString().equals("")) {
          combinedPattern = new StringBuilder(valueWrap);
        } else {
          combinedPattern.append("|").append(valueWrap);
        }
      }
    } else {
      combinedPattern = new StringBuilder(patterns.get(0));
    }

    return combinedPattern.toString();
  }
}
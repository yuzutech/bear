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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GrokProcessor {

  public static final String TYPE = "grok";
  private static final String PATTERN_MATCH_KEY = "_ingest._grok_match_index";

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

  public void execute(Event event) throws Exception {
    String fieldValue = event.getFieldValue(matchField, String.class, ignoreMissing);

    if (fieldValue == null && ignoreMissing) {
      return;
    } else if (fieldValue == null) {
      throw new IllegalArgumentException("field [" + matchField + "] is null, cannot process it.");
    }

    Map<String, Object> matches = grok.captures(fieldValue);
    if (matches == null) {
      throw new IllegalArgumentException("Provided Grok expressions do not match field value: [" + fieldValue + "]");
    }

    matches.entrySet()
        .forEach((e) -> event.setFieldValue(e.getKey(), e.getValue()));

    if (traceMatch) {
      if (matchPatterns.size() > 1) {
        @SuppressWarnings("unchecked")
        HashMap<String, String> matchMap = (HashMap<String, String>) event.getFieldValue(PATTERN_MATCH_KEY, Object.class);
        matchMap.keySet().stream().findFirst().ifPresent((index) -> {
          event.setFieldValue(PATTERN_MATCH_KEY, index);
        });
      } else {
        event.setFieldValue(PATTERN_MATCH_KEY, "0");
      }
    }
  }

  Grok getGrok() {
    return grok;
  }

  boolean isIgnoreMissing() {
    return ignoreMissing;
  }

  String getMatchField() {
    return matchField;
  }

  List<String> getMatchPatterns() {
    return matchPatterns;
  }

  static String combinePatterns(List<String> patterns, boolean traceMatch) {
    String combinedPattern;
    if (patterns.size() > 1) {
      combinedPattern = "";
      for (int i = 0; i < patterns.size(); i++) {
        String pattern = patterns.get(i);
        String valueWrap;
        if (traceMatch) {
          valueWrap = "(?<" + PATTERN_MATCH_KEY + "." + i + ">" + pattern + ")";
        } else {
          valueWrap = "(?:" + patterns.get(i) + ")";
        }
        if (combinedPattern.equals("")) {
          combinedPattern = valueWrap;
        } else {
          combinedPattern = combinedPattern + "|" + valueWrap;
        }
      }
    } else {
      combinedPattern = patterns.get(0);
    }

    return combinedPattern;
  }

  public static final class Factory {

    private final Map<String, String> builtinPatterns;

    public Factory(Map<String, String> builtinPatterns) {
      this.builtinPatterns = builtinPatterns;
    }

    public GrokProcessor create(Map<String, Object> config) throws Exception {
      Map<String, String> patternBank = new HashMap<>(builtinPatterns);
      List<String> matchPatterns = new ArrayList<>();
      String matchField = "";
      boolean traceMatch = false;
      boolean ignoreMissing = false;
      try {
        return new GrokProcessor(patternBank, matchPatterns, matchField, traceMatch, ignoreMissing);
      } catch (Exception e) {
        throw new IllegalArgumentException(TYPE + " patterns - " + "Invalid regex pattern found in: " + matchPatterns, e);
      }

    }
  }
}
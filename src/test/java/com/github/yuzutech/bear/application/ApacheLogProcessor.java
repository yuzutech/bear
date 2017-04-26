package com.github.yuzutech.bear.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.yuzutech.bear.Event;
import com.github.yuzutech.bear.Processor;
import com.github.yuzutech.bear.benchmark.ApacheLogBenchmark;

public class ApacheLogProcessor {

  private final Map<String, String> patternBank;
  private List<Processor> processors;

  public ApacheLogProcessor() throws IOException {
    this.patternBank = loadBuiltinPatterns();
    this.processors = new ArrayList<>();
    processors.add(new ApacheAccessLogProcessor(patternBank));
  }

  public Event execute(Event event) throws Exception {
    for (Processor processor : processors) {
      event = processor.execute(event);
      if (event == null) {
        return null;
      }
    }
    return event;
  }

  private static final String[] PATTERN_NAMES = new String[]{
      "grok-patterns", "custom-patterns"
  };

  public static Map<String, String> loadBuiltinPatterns() throws IOException {
    Map<String, String> builtinPatterns = new HashMap<>();
    for (String pattern : PATTERN_NAMES) {
      try (InputStream is = ApacheLogBenchmark.class.getResourceAsStream("/patterns/" + pattern)) {
        loadPatterns(builtinPatterns, is);
      }
    }
    return Collections.unmodifiableMap(builtinPatterns);
  }

  private static void loadPatterns(Map<String, String> patternBank, InputStream inputStream) throws IOException {
    String line;
    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    while ((line = br.readLine()) != null) {
      String trimmedLine = line.replaceAll("^\\s+", "");
      if (trimmedLine.startsWith("#") || trimmedLine.length() == 0) {
        continue;
      }

      String[] parts = trimmedLine.split("\\s+", 2);
      if (parts.length == 2) {
        patternBank.put(parts[0], parts[1]);
      }
    }
  }
}

package com.github.yuzutech.bear.application;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import com.github.yuzutech.bear.Event;
import com.github.yuzutech.bear.Filter;
import com.github.yuzutech.bear.GrokProcessor;

public class ApacheAccessLogFilter implements Filter {

  private GrokProcessor grokProcessor;
  private final DateTimeFormatter formatter;
  private final Pattern httpCode5xxOr4xxPattern;

  public ApacheAccessLogFilter(Map<String, String> patternBank) {
    List<String> matchPatterns = new ArrayList<>();
    matchPatterns.add("%{APACHE_ACCESS_LOG}");
    matchPatterns.add("%{APACHE_ACCESS_LOG_SIMPLE}");
    matchPatterns.add("%{APACHE_ACCESS_LOG_CONFLUENCE}");
    this.grokProcessor = new GrokProcessor(patternBank, matchPatterns, "message", false, false);
    this.formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z").withLocale(Locale.US);
    this.httpCode5xxOr4xxPattern = Pattern.compile("/[45]../");
  }

  @Override
  public void execute(Event event) throws Exception {
    if (event.equals("type", "apache-access-log")) {
      grokProcessor.execute(event);
      event.matchDate("date", formatter);
      if (event.contains("project", "abc")) {
        if (event.contains("path", "customer")) {
          event.setFieldValue("application", "crm");
        } else if (event.contains("path", "invoice")) {
          event.setFieldValue("application", "invoice");
        }
      }
      if (event.matches("response", httpCode5xxOr4xxPattern)) {
        event.setFieldValue("level", "ERROR");
      } else {
        event.setFieldValue("level", "DEBUG");
      }
    }
  }
}

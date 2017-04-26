package com.github.yuzutech.bear.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.github.yuzutech.bear.Event;
import com.github.yuzutech.bear.Processor;
import com.github.yuzutech.bear.GrokProcessor;

public class ApacheAccessLogProcessor implements Processor {

  private GrokProcessor grokProcessor;
  private final DateTimeFormatter formatter;
  private final Pattern httpCode5xxOr4xxPattern;

  public ApacheAccessLogProcessor(Map<String, String> patternBank) {
    List<String> matchPatterns = new ArrayList<>();
    matchPatterns.add("%{APACHE_ACCESS_LOG}");
    matchPatterns.add("%{APACHE_ACCESS_LOG_SIMPLE}");
    matchPatterns.add("%{APACHE_ACCESS_LOG_CONFLUENCE}");
    this.grokProcessor = new GrokProcessor(patternBank, matchPatterns, "message", false, false);
    this.formatter = DateTimeFormat.forPattern("dd/MMM/yyyy:HH:mm:ss Z").withLocale(Locale.US);
    this.httpCode5xxOr4xxPattern = Pattern.compile("[45]..");
  }

  @Override
  public Event execute(Event event) {
    if (event.equals("type", "apache-access-log")) {
      grokProcessor.execute(event);
      event.matchDate("date", formatter);
      if (event.contains("project", "abc")) {
        if (event.contains("path", "customer")) {
          event.add("application", "crm");
        } else if (event.contains("path", "invoice")) {
          event.add("application", "invoice");
        }
      }
      if (event.matches("response", httpCode5xxOr4xxPattern)) {
        event.set("level", "ERROR");
      } else {
        event.set("level", "DEBUG");
      }
    }
    return event;
  }
}

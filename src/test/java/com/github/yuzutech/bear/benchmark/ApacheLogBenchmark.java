package com.github.yuzutech.bear.benchmark;

import java.util.concurrent.TimeUnit;

import com.github.yuzutech.bear.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Timer;
import com.github.yuzutech.bear.application.ApacheLogFilter;

public class ApacheLogBenchmark {

  private static final Logger logger = LoggerFactory.getLogger(ApacheLogBenchmark.class.getName());
  private static final MetricRegistry metrics = new MetricRegistry();

  public static void main(String[] args) throws Exception {
    Timer timer = metrics.timer("rate");
    Slf4jReporter infoReporter = Slf4jReporter.forRegistry(metrics)
           .convertRatesTo(TimeUnit.SECONDS)
           .convertDurationsTo(TimeUnit.MILLISECONDS)
           .outputTo(logger)
           .build();
    infoReporter.start(5, TimeUnit.SECONDS);
    int events = 10000000;
    ApacheLogFilter filter = new ApacheLogFilter();
    for (int i = 0; i < events; i++) {
      Timer.Context processingTime = timer.time();
      Event event = new Event();
      event.setFieldValue("project", "abc");
      event.setFieldValue("type", "apache-access-log");
      event.setFieldValue("message", "163.90.205.213 [29/Nov/2012:10:22:50 +0100] 'ALPHABET' 'POST request HTTP/1.1' 123 size:6644 'dur-s:0' 'dur-ms:474627' 'vhost:vhost' 'ref:ref' 'uagent:agent' 'resp-loca:respLoca' 'tx:perftx'");
      filter.execute(event);
      processingTime.stop();
    }
  }
}

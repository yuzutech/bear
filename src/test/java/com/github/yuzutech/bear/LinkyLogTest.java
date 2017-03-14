package com.github.yuzutech.bear;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.yuzutech.bear.application.ApacheLogFilter;
import org.junit.Test;

public class LinkyLogTest {

  @Test
  public void testApacheAccessLog() throws Exception {
    Event event = new Event();
    event.set("project", "abc");
    event.set("type", "apache-access-log");
    event.set("message", "163.90.205.213 [29/Nov/2012:10:22:50 +0100] 'ALPHABET' 'POST request HTTP/1.1' 123 size:6644 'dur-s:0' 'dur-ms:474627' 'vhost:vhost' 'ref:ref' 'uagent:agent' 'resp-loca:respLoca' 'tx:perftx'");

    ApacheLogFilter filter = new ApacheLogFilter();
    filter.execute(event);

    assertThat(event.get("clientip")).isEqualTo("163.90.205.213");
    assertThat(event.get("date")).isEqualTo("29/Nov/2012:10:22:50 +0100");
    assertThat(event.get("application")).isEqualTo("ALPHABET");
    assertThat(event.get("verb")).isEqualTo("POST");
    assertThat(event.get("request")).isEqualTo("request");
    assertThat(event.get("httpversion")).isEqualTo("1.1");
    assertThat(event.get("response")).isEqualTo("123");
    assertThat(event.get("level")).isEqualTo("DEBUG");
    assertThat(event.get("bytes")).isEqualTo("6644");
    assertThat(event.get("durationsec")).isEqualTo("0");
    assertThat(event.get("durationmillisec")).isEqualTo("474627");
    assertThat(event.get("vhost")).isEqualTo("vhost");
    assertThat(event.get("referrer")).isEqualTo("ref");
    assertThat(event.get("agent")).isEqualTo("agent");
    assertThat(event.get("location")).isEqualTo("respLoca");
    assertThat(event.get("perftx")).isEqualTo("perftx");
  }
}

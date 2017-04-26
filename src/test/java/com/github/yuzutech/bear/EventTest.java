package com.github.yuzutech.bear;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.format.DateTimeFormat;
import org.junit.Test;

public class EventTest {

  @Test
  public void should_add_tag_to_list() {
    Event event = new Event();
    event.set("tags", new ArrayList<>(Arrays.asList("kafka", "elasticsearch")));
    event.add("tags", "bear");
    assertThat(event.get("tags", List.class)).contains("kafka", "elasticsearch", "bear");
  }

  @Test
  public void should_set_value() {
    Map<String, Object> data = new HashMap<>();
    data.put("foo", "bar");
    Event event = new Event(data);
    Event result = event.set("foo", "qux");
    assertThat(event.get("foo")).isEqualTo("qux");
    assertThat(result.get("foo")).isEqualTo("qux");
  }

  @Test
  public void should_throw_class_cast_exception_if_type_mismatch() {
    Map<String, Object> data = new HashMap<>();
    data.put("value", "0");
    Event event = new Event(data);
    assertThat(event.get("value")).isEqualTo("0");
    assertThatThrownBy(() -> event.get("value", Integer.class))
        .isInstanceOf(ClassCastException.class)
        .hasMessageContaining("field [value] of type [java.lang.String]");
  }

  @Test
  public void should_return_true_if_field_exists() {
    Map<String, Object> data = new HashMap<>();
    data.put("data", "abc");
    Event event = new Event(data);
    assertThat(event.hasField("data")).isTrue();
  }

  @Test
  public void should_return_false_if_field_does_not_exist() {
    Map<String, Object> data = new HashMap<>();
    data.put("data", "abc");
    Event event = new Event(data);
    assertThat(event.hasField("yolo")).isFalse();
  }

  @Test
  public void should_return_the_first_element_or_the_value() {
    Event event = new Event();
    event.set("tags", new ArrayList<>(Arrays.asList("kafka", "elasticsearch")));
    event.set("data", "abc");
    String firstTag = event.getFirst("tags", String.class);
    String firstData = event.getFirst("data", String.class);
    assertThat(firstTag).isEqualTo("kafka");
    assertThat(firstData).isEqualTo("abc");
  }

  @Test
  public void should_update_value() {
    Event event = new Event();
    event.set("data", "abc");
    event.update("data", String::toUpperCase);
    assertThat(event.get("data")).isEqualTo("ABC");
  }

  @Test
  public void should_not_update_null_value() {
    Event event = new Event();
    event.set("data", "abc");
    event.update("notexists", String::toUpperCase);
    assertThat(event.get("data")).isEqualTo("abc");
  }

  @Test
  public void should_remove_value() {
    Event event = new Event();
    event.set("data", "abc");
    assertThat(event.get("data")).isEqualTo("abc");
    event.remove("data");
    assertThat(event.get("data")).isNull();
  }

  @Test
  public void should_convert_to_int() {
    Event event = new Event();
    event.set("data", "0");
    assertThat(event.get("data")).isEqualTo("0");
    event.toInt("data");
    assertThat(event.getInt("data")).isEqualTo(0);
  }

  @Test
  public void should_throw_a_number_format_when_cast_string_to_int() {
    Event event = new Event();
    event.set("data", "abc");
    assertThat(event.get("data")).isEqualTo("abc");
    assertThatThrownBy(() -> event.toInt("data"))
        .isInstanceOf(NumberFormatException.class);
  }

  @Test
  public void should_convert_a_double_to_int() {
    Event event = new Event();
    event.set("data", "1.25");
    assertThat(event.get("data")).isEqualTo("1.25");
    event.toInt("data");
    assertThat(event.getInt("data")).isEqualTo(1);
  }

  @Test
  public void should_match_date() {
    Event event = new Event();
    event.set("date", "Wed Dec 10 14:17:10 2014");
    event.matchDate("date", DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss yyyy").withLocale(Locale.US));
    assertThat(event.get("timestamp")).isEqualTo("2014-12-10T14:17:10.000");
  }

  @Test
  public void should_match_dates() {
    Event event = new Event();
    event.set("date", "Wed Dec 10 14:17:10 2014");
    event.matchDate("date", Arrays.asList(
        DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss yyyy").withLocale(Locale.US),
        DateTimeFormat.forPattern("dd/MMM/yyyy:HH:mm:ss Z").withLocale(Locale.US)));
    assertThat(event.get("timestamp")).isEqualTo("2014-12-10T14:17:10.000");
  }

  @Test
  public void should_match_pattern() {
    Event event = new Event();
    event.set("response", "404 Not Found");

    Pattern httpCode5xxOr4xxPattern = Pattern.compile("^([45]..) ([a-zA-Z ]+)");
    Matcher match = event.match("response", httpCode5xxOr4xxPattern);
    assertThat(match.matches()).isTrue();
    assertThat(match.group(1)).isEqualTo("404");
    assertThat(match.group(2)).isEqualTo("Not Found");

    match = event.match("missing", httpCode5xxOr4xxPattern);
    assertThat(match).isNull();
  }

  @Test
  public void should_matches_pattern() {
    Event event = new Event();
    event.set("response", "404");

    Pattern httpCode5xxOr4xxPattern = Pattern.compile("^[45]..");
    assertThat(event.matches("response", httpCode5xxOr4xxPattern)).isTrue();

    event = new Event();
    event.set("response", "500");
    assertThat(event.matches("response", httpCode5xxOr4xxPattern)).isTrue();

    event = new Event();
    event.set("response", "200");
    assertThat(event.matches("response", httpCode5xxOr4xxPattern)).isFalse();
  }
}

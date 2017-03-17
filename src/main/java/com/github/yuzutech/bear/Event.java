package com.github.yuzutech.bear;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;

public class Event {

  public Map<String, Object> data = new HashMap<>();

  public Event() {
  }

  public Event(Map<String, Object> data) {
    this.data = data;
  }

  // get

  public String get(String key) {
    return get(key, String.class);
  }

  public Integer getInt(String key) {
    return get(key, Integer.class);
  }

  public <T> T get(String key, Class<T> clazz) {
    Object value = data.get(key);
    if (value == null) {
      return null;
    }
    if (clazz.isInstance(value)) {
      return clazz.cast(value);
    }
    throw new ClassCastException("field [" + key + "] of type [" + value.getClass().getName() + "] cannot be cast to [" + clazz.getName() + "]");
  }

  public boolean hasField(String key) {
    return data.get(key) != null;
  }

  @SuppressWarnings("unchecked")
  public <T> T getFirst(String key, Class<T> clazz) {
    try {
      Collection values = get(key, Collection.class);
      if (values != null) {
        return (T) values.stream().findFirst().orElse(null);
      }
      return null;
    } catch (ClassCastException cce) {
      return get(key, clazz);
    }
  }

  // add

  public Event add(String key, String value) {
    add(key, value, String.class);
    return this;
  }

  @SuppressWarnings("unchecked")
  public <T> Event add(String key, T value, Class<T> clazz) {
    try {
      Collection values = get(key, Collection.class);
      if (values != null) {
        values.add(value);
      } else {
        data.put(key, value);
      }
    } catch (ClassCastException e) {
      T currentValue = get(key, clazz);
      List<T> result = new ArrayList<>();
      result.add(currentValue);
      result.add(value);
      data.put(key, result);
    }
    return this;
  }

  // set

  public <T> Event set(String key, T value) {
    data.put(key, value);
    return this;
  }

  // update

  public Event update(String key, Function<String, String> transform) {
    return update(key, String.class, transform);
  }

  public <T> Event update(String key, Class<T> clazz, Function<T, T> transform) {
    T value = get(key, clazz);
    if (value == null) {
      return this;
    }
    data.put(key, transform.apply(value));
    return this;
  }

  // remove

  public Event remove(String key) {
    data.remove(key);
    return this;
  }

  // convert

  public void toInt(String key) {
    String value = get(key, String.class);
    if (value != null) {
      set(key, Double.valueOf(value).intValue());
    }
  }

  // function

  public boolean equals(String key, String value) {
    return value.equals(get(key));
  }

  public boolean contains(String key, String value) {
    try {
      Collection values = get(key, Collection.class);
      return values != null && values.contains(value);
    } catch (ClassCastException e) {
      return get(key).contains(value);
    }
  }

  public void matchDate(String key, DateTimeFormatter formatter) {
    String fieldValue = get(key, String.class);
    if (fieldValue != null) {
      try {
        LocalDateTime dateTime = formatter.parseLocalDateTime(fieldValue);
        data.put("timestamp", dateTime.toString());
      } catch (IllegalArgumentException e) {
        // Ignore...
      }
    }
  }

  public void matchDate(String key, List<DateTimeFormatter> formatters) {
    String fieldValue = get(key, String.class);
    if (fieldValue != null) {
      for (DateTimeFormatter formatter : formatters) {
        try {
          LocalDateTime dateTime = formatter.parseLocalDateTime(fieldValue);
          data.put("timestamp", dateTime.toString());
        } catch (IllegalArgumentException e) {
          // Ignore...
        }
      }

    }
  }

  public boolean matches(String key, Pattern pattern) {
    String fieldValue = get(key, String.class);
    return fieldValue != null && pattern.matcher(fieldValue).matches();
  }

  public Matcher match(String key, Pattern pattern) {
    String data = get(key, String.class);
    if (data != null && !data.isEmpty()) {
      return pattern.matcher(data);
    }
    return null;
  }
}

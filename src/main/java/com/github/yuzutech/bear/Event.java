package com.github.yuzutech.bear;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Event {

  public Map<String, Object> data = new HashMap<>();

  public Event() {
  }

  public Event(Map<String, Object> data) {
    this.data = data;
  }

  public void setFieldValue(String key, Object value) {
    data.put(key, value);
  }

  public <T> T getFieldValue(String key, Class<T> clazz) {
    Object value = data.get(key);
    if (value == null) {
      throw new NullPointerException("field [" + key + "] does not exist");
    }
    if (clazz.isInstance(value)) {
      return clazz.cast(value);
    }
    throw new IllegalArgumentException("field [" + key + "] of type [" + value.getClass().getName() + "] cannot be cast to [" + clazz.getName() + "]");
  }

  public <T> T getFieldValue(String key, Class<T> clazz, boolean ignoreMissing) {
    try {
      return getFieldValue(key, clazz);
    } catch (NullPointerException e) {
      if (ignoreMissing) {
        return null;
      } else {
        throw e;
      }
    }
  }

  private boolean hasField(String key) {
    return data.get(key) != null;
  }

  public boolean equals(String key, String value) {
    return value.equals(get(key));
  }

  public boolean contains(String key, String value) {
    try {
      List fieldValues = getFieldValue(key, ArrayList.class, true);
      if (fieldValues == null) {
        return false;
      }
      return fieldValues.contains(value);
    } catch (IllegalArgumentException e) {
      String fieldValue = get(key);
      return fieldValue.contains(value);
    }
  }

  public String get(String key) {
    return getFieldValue(key, String.class);
  }

  public void matchDate(String key, DateTimeFormatter formatter) {
    String fieldValue = getFieldValue(key, String.class, false);
    LocalDateTime dateTime = LocalDateTime.parse(fieldValue, formatter);
    setFieldValue("timestamp", dateTime.toString());
  }

  public boolean matches(String key, Pattern pattern) {
    String fieldValue = getFieldValue(key, String.class);
    return pattern.matcher(fieldValue).matches();
  }
}

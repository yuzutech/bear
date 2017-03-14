package com.github.yuzutech.bear;

public interface Filter {

  Event execute(Event event) throws Exception;
}

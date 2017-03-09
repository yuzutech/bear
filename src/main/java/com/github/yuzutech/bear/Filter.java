package com.github.yuzutech.bear;

public interface Filter {

  void execute(Event event) throws Exception;
}

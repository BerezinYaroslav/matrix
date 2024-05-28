package ru.cinimex.springReflection.entity;

import ru.cinimex.springReflection.annotation.StringValue;

public class Parent {
  @StringValue("Parent Value")
  private String name;

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "Parent{" +
        "name='" + name + '\'' +
        '}';
  }
}

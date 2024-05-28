package ru.cinimex.springReflection.entity;

import ru.cinimex.springReflection.annotation.InjectBean;
import ru.cinimex.springReflection.annotation.StringValue;

public class Child {

  @InjectBean
  private Parent parent;
  @StringValue("Child Value")
  private String name;

  public Parent getParent() {
    return parent;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "Child{" +
        "parent=" + parent +
        '}';
  }
}

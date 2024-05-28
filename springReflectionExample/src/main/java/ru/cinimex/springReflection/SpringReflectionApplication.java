package ru.cinimex.springReflection;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.cinimex.springReflection.entity.Child;

@SpringBootApplication
public class SpringReflectionApplication {

  public static void main(String[] args) {
    BeanContext beanContext = new BeanContext();
    Child bean = beanContext.getBean(Child.class);
    System.out.println(bean);
  }
}

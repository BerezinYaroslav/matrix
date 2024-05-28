# Custom Spring Boot Starter

This is a custom starter for Spring Boot that provides HelloWorld additional functionality.

## Configuration

To use this starter, you can include the following dependency in your `pom.xml` file:

```xml
<dependency>
    <groupId>ru.cinimex</groupId>
    <artifactId>customSpringBootStarter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

This starter provides some additional configuration options that you can use to customize its behavior.
You can configure these options in your application.yml file:

```yml
ru:
  cinimex:
    customstarterdemo:
      enabled: true
      name: "Some name to say hello"
```

Note: the starter functionality does not work without the 'ru.cinimex.customstarterdemo.enabled=true' property

## Usage

You can autowire starter bean to say hello:

```java
@Autowired
private StarterDemo demo;
```

Also, you can apply annotation @ExecutionTime to log method execution time.
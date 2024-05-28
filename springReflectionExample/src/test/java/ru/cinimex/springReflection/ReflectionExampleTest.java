package ru.cinimex.springReflection;

import org.junit.jupiter.api.Test;
import org.springframework.util.ReflectionUtils;
import ru.cinimex.springReflection.annotation.StringValue;
import ru.cinimex.springReflection.entity.Parent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class ReflectionExampleTest {

    @Test
    public void reflectionExampleOne() {
        Class<Parent> clazz = Parent.class;
        printFieldsClass(clazz);
        printMethodsClass(clazz);
    }

    public static String getFieldInfo(Field field) {
        StringValue annotation = field.getAnnotation(StringValue.class);
        String annotationName = annotation.annotationType().getName();
        String value = annotation.value();
        String modifier = Modifier.isPrivate(field.getModifiers()) ? "private" : "not private";
        String typeName = field.getType().getName();
        String name = field.getName();
        return new StringJoiner(" ")
                .add(annotationName)
                .add("(" + value + ")")
                .add(modifier)
                .add(typeName)
                .add(name)
                .toString();
    }

    private static String getMethodsInfo(Method method) {
        String modifier = Modifier.isPublic(method.getModifiers()) ? "public" : "not public";
        String returnTypeName = method.getReturnType().getName();
        String name = method.getName();
        String params =
                Arrays.stream(method.getParameterTypes())
                        .map(Class::getName)
                        .collect(Collectors.joining(", ", "(", ")"));
        return new StringJoiner(" ").add(modifier).add(returnTypeName).add(name).add(params).toString();
    }

    private static void printFieldsClass(Class clazz){
        System.out.println("-------------Fields---------------");
        Arrays.stream(clazz.getDeclaredFields())
                .map(ReflectionExampleTest::getFieldInfo)
                .forEach(System.out::println);
    }

    private static void printMethodsClass(Class clazz){
        System.out.println("-------------Methods--------------");
        Method[] allDeclaredMethods = ReflectionUtils.getDeclaredMethods(clazz);
        Arrays.stream(allDeclaredMethods)
                .map(ReflectionExampleTest::getMethodsInfo)
                .forEach(System.out::println);
    }
}

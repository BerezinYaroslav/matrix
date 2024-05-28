package ru.cinimex.springReflection;

import ru.cinimex.springReflection.annotation.InjectBean;
import ru.cinimex.springReflection.annotation.StringValue;
import ru.cinimex.springReflection.utils.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BeanContext {
    private final Map<String, Object> contextMap = new HashMap<>();
    private final ReflectionUtils fieldUtils = new ReflectionUtils();

    public <T> T getBean(Class<T> clazz) {
        String className = clazz.getName();

        if (contextMap.containsKey(className)) {
            return (T) contextMap.get(className);
        }

        T instance = newInstanceOfBean(clazz);

        Arrays.stream(clazz.getDeclaredFields())
                .forEach(field -> setFieldValue(instance, field));

        //TODO реализовать аннотацию для методов, которая будет
        // логировать входящие значения аргументов у метода при его вызове
        // https://restless-man.livejournal.com/24320.html
        // проверить работоспособность аннотации на вызове любого метода с двумя аргументами разного типа

        contextMap.put(className, instance);
        return instance;
    }

    private <T> T newInstanceOfBean(Class<T> clazz) {
        try {
            return clazz.newInstance();
            //TODO добавить аннотацию для указания какой именно конструктор вызывать и реализовать метод для его вызова
            // https://www.tutorialspoint.com/java/lang/class_getconstructor.htm
            // Добавить конструктор с 2 параметрами в класс Child и инициализировать объект через него
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Object getOrCreateBean(Class<?> clazz) {
        if (!contextMap.containsKey(clazz.getName())) {
            return getBean(clazz);
        } else {
            return contextMap.get(clazz.getName());
        }
    }

    private void setFieldValue(Object instance, Field field) {
        Optional.ofNullable(field.getAnnotation(StringValue.class))
                .ifPresent(annotation -> fieldUtils.setField(instance, field, annotation.value()));

        InjectBean injectBeanAnnotation = field.getAnnotation(InjectBean.class);
        if (injectBeanAnnotation != null) {
            Object fieldInstance = getOrCreateBean(field.getType());
            fieldUtils.setField(instance, field, fieldInstance);
        }
    }
}

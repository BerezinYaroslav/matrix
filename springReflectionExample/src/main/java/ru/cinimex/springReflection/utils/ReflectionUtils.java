package ru.cinimex.springReflection.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ReflectionUtils {
    public void setField(Object instance, Field field, Object value) {
        if (Modifier.isPrivate(field.getModifiers())) {
            setPrivateFieldValue(instance, field, value);
        } else {
            setNonPrivateFieldValue(instance, field, value);
        }
    }

    private void setPrivateFieldValue(Object instance, Field field, Object value) {
        field.setAccessible(true);
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            field.setAccessible(false);
        }
    }

    private void setNonPrivateFieldValue(Object instance, Field field, Object value) {
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}

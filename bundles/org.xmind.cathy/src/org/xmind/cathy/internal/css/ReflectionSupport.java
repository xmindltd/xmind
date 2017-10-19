package org.xmind.cathy.internal.css;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionSupport<T> {

    private Class<?> type;

    public ReflectionSupport(Class<T> type) {
        this.type = type;
    }

    public Object getFieldValue(Field field, T instance) {
        Object value = null;
        if (field != null) {
            boolean accessible = field.isAccessible();
            try {
                field.setAccessible(true);
                value = field.get(instance);
            } catch (Exception exc) {
                // do nothing
            } finally {
                field.setAccessible(accessible);
            }
        }
        return value;
    }

    public Object getFieldValue(String name, T instance) {
        Field field = getField(name);
        return getFieldValue(field, instance);
    }

    public Field getField(String name) {
        while (!type.equals(Object.class)) {
            try {
                return type.getDeclaredField(name);
            } catch (Exception exc) {
                type = type.getSuperclass();
            }
        }
        return null;
    }

    public Object set(Object obj, String name, Object value) {
        try {
            Field field = getField(name);
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            field.set(obj, value);
            field.setAccessible(accessible);
            return value;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object executeMethod(Method method, T instance, Object... params) {
        Object value = null;
        if (method != null) {
            boolean accessible = method.isAccessible();
            try {
                method.setAccessible(true);
                value = method.invoke(instance, params);
            } catch (Exception exc) {
                // do nothing
            } finally {
                method.setAccessible(accessible);
            }
        }
        return value;
    }

    public Method getMethod(String name, Class<?>... params) {
        while (!type.equals(Object.class)) {
            try {
                return type.getDeclaredMethod(name, params);
            } catch (Exception exc) {
                type = type.getSuperclass();
            }
        }
        return null;
    }

}

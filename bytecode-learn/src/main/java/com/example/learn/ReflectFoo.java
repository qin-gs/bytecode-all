package com.example.learn;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectFoo {

    private static int count = 0;

    public static void foo() {
        new Exception("test# " + (count++)).printStackTrace();
    }

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<ReflectFoo> clazz = ReflectFoo.class;
        Method method = clazz.getMethod("foo");
        for (int i = 0; i < 20; i++) {
            method.invoke(null);
        }
    }
}

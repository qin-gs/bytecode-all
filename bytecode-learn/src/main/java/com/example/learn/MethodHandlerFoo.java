package com.example.learn;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * 方法句柄
 */
public class MethodHandlerFoo {
    public void print(String s) {
        System.out.println("hello " + s);
    }

    public static void main(String[] args) throws Throwable {
        MethodHandlerFoo foo = new MethodHandlerFoo();
        // 方法签名：指定返回值 和 参数类型
        MethodType methodType = MethodType.methodType(void.class, String.class);
        // 查找指定方法签名的方法句柄
        MethodHandle methodHandle = MethodHandles.lookup().findVirtual(MethodHandlerFoo.class, "print", methodType);
        // 使用方法句柄调用方法
        methodHandle.invokeExact(foo, "world");
    }

}

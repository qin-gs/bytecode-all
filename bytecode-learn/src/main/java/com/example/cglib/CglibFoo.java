package com.example.cglib;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class CglibFoo {

    public static void main(String[] args) {
        MethodInterceptor interceptor = new MethodInterceptor() {
            @Override
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                System.out.println("before ---");
                Object o = proxy.invokeSuper(obj, args);
                System.out.println("after ---");
                return o;
            }
        };
        Bar bar = (Bar) Enhancer.create(Bar.class, interceptor);
        bar.doSomething("something");
    }
}

class Bar {
    public void doSomething(String job) {
        System.out.println("class name: " + getClass());
        System.out.println("job name: " + job);
    }

    public void eat() {

    }
 }
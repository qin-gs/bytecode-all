package com.example.attach;

import java.util.concurrent.TimeUnit;

public class AttachFoo {

    public static void main(String[] args) throws InterruptedException {
        while (true) {
            System.out.println(foo());
            TimeUnit.SECONDS.sleep(2);
        }
    }

    public static int foo() {
        return 100;
    }
}

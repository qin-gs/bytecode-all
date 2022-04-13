package com.example.javac;

import com.sun.tools.javac.parser.Scanner;
import com.sun.tools.javac.parser.ScannerFactory;
import com.sun.tools.javac.util.Context;

public class ScannerFoo {

    public static void main(String[] args) {
        ScannerFactory factory = ScannerFactory.instance(new Context());
        // 词法分析
        Scanner scanner = factory.newScanner("int k = i + j; ", false);

        scanner.nextToken();
        System.out.println(scanner.token().kind); // int
        scanner.nextToken();
        System.out.println(scanner.token().kind); // k
        scanner.nextToken();
        System.out.println(scanner.token().kind); // =
        scanner.nextToken();
        System.out.println(scanner.token().kind); // i
        scanner.nextToken();
        System.out.println(scanner.token().kind); // +
        scanner.nextToken();
        System.out.println(scanner.token().kind); // j
        scanner.nextToken();
        System.out.println(scanner.token().kind); // ;

    }

}

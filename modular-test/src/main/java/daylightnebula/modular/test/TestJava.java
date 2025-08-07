package daylightnebula.modular.test;

import daylightnebula.modular.annotations.OnStartup;

public class TestJava {
    @OnStartup
    public static void onStartup() {
        System.out.println("Java startup!");
    }
}

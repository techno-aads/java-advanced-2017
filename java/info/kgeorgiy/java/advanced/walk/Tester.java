package info.kgeorgiy.java.advanced.walk;

import info.kgeorgiy.java.advanced.base.BaseTester;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class Tester extends BaseTester {
    public static void main(final String... args) throws NoSuchAlgorithmException, IOException {
        /*
         * How to run
         * IDEA — Edit configuration — Program arguments
         * RecursiveWalk ru.ifmo.ctddev.solutions.walk.RecursiveWalk
         *
         * Note: Запуск через IDEA не генерирует сертификат — это ОК
         */

        new Tester()
                .add("Walk", WalkTest.class)
                .add("RecursiveWalk", RecursiveWalkTest.class)
                .run(args);
    }
}

package info.kgeorgiy.java.advanced.arrayset;

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
         * NavigableSet ru.ifmo.ctddev.solutions.arrayset.ArraySet
         *
         * Note: Запуск через IDEA не генерирует сертификат — это ОК
         */
        new Tester()
                .add("SortedSet", SortedSetTest.class)
                .add("NavigableSet", NavigableSetTest.class)
                .run(args);
    }
}

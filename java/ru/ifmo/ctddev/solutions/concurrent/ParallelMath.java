package ru.ifmo.ctddev.solutions.concurrent;

import javafx.util.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public final class ParallelMath {

    private ParallelMath() {
    }

    @FunctionalInterface
    public interface Job<T> {
        T perform(int form, int to);
    }

    public static <T, U> List<U> map(List<? extends T> values, Function<? super T, ? extends U> f, int threadCount) throws InterruptedException {

        Job<List<U>> job = (from, to) -> map(from, to, values, f);
        List<List<U>> results = reduce(job, threadCount, values.size());

        List<U> result = new ArrayList<>();
        results.forEach(result::addAll);

        return result;
    }

    public static <T> List<T> reduce(Job<T> job, int threadCount, int loopLength) throws InterruptedException {

        ThreadsPull.Builder threadPullBuilder = new ThreadsPull.Builder();
        T[] totals = (T[]) new Object[threadCount];
        for (int i = 0; i < threadCount; i++) {

            Pair<Integer, Integer> bounds = calculateBounds(i, threadCount, loopLength);
            final int from = bounds.getKey();
            final int to = bounds.getValue();
            final int threadIndex = i;

            Thread thread = new Thread(() -> {
                totals[threadIndex] = job.perform(from, to);
            });
            thread.start();
            threadPullBuilder.add(thread);
        }

        threadPullBuilder.build().join();
        return Arrays.asList(totals);
    }

    public static Pair<Integer, Integer> calculateBounds(int jobIndex, int jobCount, int loopLength) {

        int chunk = loopLength / jobCount;
        int rest = loopLength % jobCount;

        int from = jobIndex * chunk;
        int to = (jobIndex + 1) * chunk;

        int lastJobIndex = jobCount - 1;
        if (jobIndex == lastJobIndex) {
            to += rest;
        }

        return new Pair<>(from, to);
    }

    public static <T, U> List<U> map(int from, int to, List<? extends T> values, Function<? super T, ? extends U> f) {
        List<U> result = new ArrayList<>();
        for (int i = from; i < to; i++) {
            T value = values.get(i);
            result.add(f.apply(value));
        }
        return result;
    }


    public static <T> Boolean all(int from, int to, List<? extends T> values, Predicate<? super T> predicate) {
        for (int i = from; i < to; i++) {
            T value = values.get(i);
            if (!predicate.test(value)) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

}
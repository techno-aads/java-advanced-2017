package ru.ifmo.ctddev.solutions.concurrent;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;

public class IterativeParallelism implements ScalarIP {

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        Function<List<? extends T>, T> function = list -> Collections.max(list, comparator);
        List<T> listMax = runInParallel(threads, values, function);
        System.out.println(listMax);
        return Collections.max(listMax, comparator);
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        Function<List<? extends T>, T> function = list -> Collections.min(list, comparator);
        List<T> listMin = runInParallel(threads, values, function);
        return Collections.min(listMin, comparator);
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        Function<List<? extends T>, Boolean> function = list -> list.stream().allMatch(predicate);
        return runInParallel(threads, values, function).stream().allMatch(value -> value);
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        Function<List<? extends T>, Boolean> function = list -> list.stream().anyMatch(predicate);
        return runInParallel(threads, values, function).stream().anyMatch(value -> value);
    }

    private <T, U> List<U> runInParallel (int maxAmount, List<? extends T> list, Function<List<? extends T>, U> function) throws InterruptedException {
        int elementsAmount = list.size();

        int threadsAmount = elementsAmount < maxAmount
                ? elementsAmount
                : maxAmount;

        List<Thread> threads = new ArrayList<>();
        List<U> results = new ArrayList<>(Collections.nCopies(threadsAmount, null));
        int step = elementsAmount / threadsAmount;

        for (int i = 0; i < threadsAmount; i++) {
            int threadIndex = i;
            Thread thread = new Thread(() -> {
                int fromIndex = step * threadIndex;
                int toIndex = threadIndex == threadsAmount - 1
                        ? elementsAmount
                        : fromIndex + step;

                results.set(threadIndex, function.apply(list.subList(fromIndex, toIndex)));
            });

            thread.start();
            threads.add(thread);
        }

        for (Thread thread: threads) {
            thread.join();
        }

        return results;
    }
}

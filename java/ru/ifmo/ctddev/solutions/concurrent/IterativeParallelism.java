package ru.ifmo.ctddev.solutions.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class IterativeParallelism implements ListIP {
    private static final int MAX_THREADS = Runtime.getRuntime().availableProcessors();

    private static <T> Function<List<List<T>>, List<T>> getFlatFunction() {
        return ts -> ts.stream().flatMap(Collection::stream).collect(toList());
    }

    private static <T> Function<List<T>, Boolean> getAllMatchFunction(Predicate<? super T> predicate) {
        return ts -> ts.stream().allMatch(predicate);
    }

    private static <T> Function<List<T>, Boolean> getAnyMatchFunction(Predicate<? super T> predicate) {
        return ts -> ts.stream().anyMatch(predicate);
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        Function<List<T>, T> maxOrNull = e -> e.stream().max(comparator).orElse(null);
        return executeFunctions(threads, values, maxOrNull, maxOrNull);
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        Function<List<T>, T> minOrNull = e -> e.stream().min(comparator).orElse(null);
        return executeFunctions(threads, values, minOrNull, minOrNull);
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return executeFunctions(threads,
                values,
                getAllMatchFunction(predicate),
                getAllMatchFunction(Predicate.isEqual(true)));
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return executeFunctions(threads,
                values,
                getAnyMatchFunction(predicate),
                getAnyMatchFunction(Predicate.isEqual(true)));
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return executeFunctions(threads,
                values,
                ts -> ts.stream().map(Object::toString).collect(Collectors.joining("")),
                results -> results.stream().collect(joining("")));
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return executeFunctions(threads,
                values,
                ts -> ts.stream().filter(predicate).collect(toList()),
                getFlatFunction());
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return executeFunctions(threads,
                values,
                ts -> ts.stream().map(f::apply).collect(toList()),
                getFlatFunction());
    }

    private<T, R, F> F executeFunctions(int threads, List<? extends T> values, Function<List<T>, R> taskFunction, Function<List<R>, F> mergeFunction) {
        List<Task<T, R>> tasks = calcTasks(threads, values, taskFunction);
        List<R> results = runTasks(tasks);
        return mergeFunction.apply(results);
    }

    private <T, R> List<Task<T, R>> calcTasks(int threads, List<? extends T> values, Function<List<T>, R> function) {
        int listSize = values.size();
        int actualThreads = Math.min(Math.min(MAX_THREADS, threads), listSize);
        int batchSize = listSize / actualThreads;

        List<Task<T, R>> tasks = new ArrayList<>();
        for (int i = 0; i < actualThreads; i++) {
            int to = (i + 1) == actualThreads ? listSize : (i + 1) * batchSize;
            tasks.add(new Task(values.subList(i * batchSize, to), function));
        }

        return tasks;
    }

    private <T, R> List<R> runTasks(List<Task<T, R>> tasks) {
        tasks.forEach(Thread::start);
        for (Task task : tasks) {
            try {
                task.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return tasks.stream()
                .map(Task::getResult)
                .collect(Collectors.toList());
    }

    private class Task<T, R> extends Thread {
        private List<T> values;
        private Function<List<T>, R> function;
        private R result;

        Task(List<T> values, Function<List<T>, R> function) {
            this.values = values;
            this.function = function;
        }

        @Override
        public void run() {
            result = function.apply(values);
        }

        public R getResult() {
            return result;
        }
    }
}

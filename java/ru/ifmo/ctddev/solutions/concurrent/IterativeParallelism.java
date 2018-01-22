package ru.ifmo.ctddev.solutions.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class IterativeParallelism implements ListIP {
    private static final int MAX_THREADS = Runtime.getRuntime().availableProcessors();
    private ParallelMapper parallelMapper;

    public IterativeParallelism() {

    }

    public IterativeParallelism(ParallelMapper parallelMapper) {
        this.parallelMapper = parallelMapper;
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return executeFunctions(
                threads,
                values,
                e -> Collections.singletonList(e.stream().max(comparator).orElse(null)),
                e -> e.stream().max(comparator).orElse(null));
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return executeFunctions(
                threads,
                values,
                e -> Collections.singletonList(e.stream().min(comparator).orElse(null)),
                e -> e.stream().min(comparator).orElse(null));
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return executeFunctions(threads,
                values,
                ts -> Collections.singletonList(ts.stream().allMatch(predicate)),
                ts -> ts.stream().allMatch(Predicate.isEqual(true)));
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return executeFunctions(threads,
                values,
                ts -> Collections.singletonList(ts.stream().anyMatch(predicate)),
                ts -> ts.stream().anyMatch(Predicate.isEqual(true)));
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return executeFunctions(threads,
                values,
                ts -> Collections.singletonList(ts.stream().map(Object::toString).collect(Collectors.joining(""))),
                results -> results.stream().collect(joining("")));
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return executeFunctions(threads,
                values,
                ts -> ts.stream().filter(predicate).collect(toList()),
                Function.identity());
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return executeFunctions(threads,
                values,
                ts -> ts.stream().map(f::apply).collect(toList()),
                Function.identity());
    }

    private <T, R, F> F executeFunctions(int threads, List<T> values, Function<List<T>, List<R>> taskFunction, Function<List<R>, F> mergeFunction) {
        if (parallelMapper != null) {
            List<List<T>> tasks = splitValues(threads, values);
            try {
                List<List<R>> results = parallelMapper.map(taskFunction, tasks);
                return mergeFunction.apply(results.stream().flatMap(Collection::stream).collect(toList()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        List<Task<T, List<R>>> tasks = splitTasks(threads, values, taskFunction);
        List<R> results = runTasks(tasks);
        return mergeFunction.apply(results);
    }

    private <T> List<List<T>> splitValues(int threads, List<T> values) {
        int listSize = values.size();
        int actualThreads = Math.min(Math.min(MAX_THREADS, threads), listSize);
        int batchSize = listSize / actualThreads;

        List<List<T>> splitValues = new ArrayList<>();
        for (int i = 0; i < actualThreads; i++) {
            int to = (i + 1) == actualThreads ? listSize : (i + 1) * batchSize;
            splitValues.add(values.subList(i * batchSize, to));
        }

        return splitValues;
    }

    private <T, R> List<Task<T, R>> splitTasks(int threads, List<T> values, Function<List<T>, R> function) {
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

    private <T, R> List<R> runTasks(List<Task<T, List<R>>> tasks) {
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
                .flatMap(Collection::stream)
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

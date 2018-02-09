package ru.ifmo.ctddev.solutions.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.concurrent.ScalarIP;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class IterativeParallelism implements ScalarIP, ListIP {
    @Override
    public <T> T maximum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        Function<List<T>, Aggregator<T>> f = (source) -> {
            Aggregator<T> agg = new Aggregator<>();

            for (int k = 0; k < source.size(); k++, agg.count++) {
                T value = source.get(k);
                agg.value = k == 0 ? value : comparator.compare(agg.value, value) >= 0 ? agg.value : value;
            }

            return agg;
        };
        List<T> result = execute(i, list, f).stream()
                .filter(e -> e.count != 0)
                .map(value -> value.value)
                .collect(Collectors.toList());

        return result.size() > 0 ? f.apply(result).value : null;
    }

    @Override
    public <T> T minimum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        Function<List<T>, Aggregator<T>> f = (source) -> {
            Aggregator<T> agg = new Aggregator<>();

            for (int k = 0; k < source.size(); k++, agg.count++) {
                agg.value = k == 0 ? source.get(k) : comparator.compare(agg.value, source.get(k)) <= 0 ? agg.value : source.get(k);
            }

            return agg;
        };

        List<T> result = execute(i, list, f).stream()
                .filter(v -> v.count != 0)
                .map(v -> v.value)
                .collect(Collectors.toList());

        return result.size() > 0 ? f.apply(result).value : null;
    }

    @Override
    public <T> boolean all(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return execute(i, list, (source) -> {
            Boolean result = null;

            for (int k = 0; k < source.size(); k++) {
                T value = source.get(k);
                result = k == 0 ? predicate.test(value) : result && predicate.test(value);
            }

            return result;
        }).stream().filter(Objects::nonNull).reduce(true, Boolean::logicalAnd);
    }

    @Override
    public <T> boolean any(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return execute(i, list, (source) -> {
            Boolean result = null;

            for (int k = 0; k < source.size(); k++) {
                T value = source.get(k);
                result = k == 0 ? predicate.test(value) : result || predicate.test(value);
            }

            return result;
        }).stream().filter(Objects::nonNull).reduce(false, Boolean::logicalOr);
    }

    @Override
    public String join(int i, List<?> list) throws InterruptedException {
        List<String> result = execute(i, list, (source) -> {
            StringBuilder stringBuilder = new StringBuilder();

            source.forEach(stringBuilder::append);

            return stringBuilder.toString();
        });

        return String.join("", result);
    }

    @Override
    public <T> List<T> filter(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return execute(i, list, (source) -> {
            List<T> results = new LinkedList<>();

            for (T value : source) if (predicate.test(value)) results.add(value);

            return results;
        }).stream().reduce(new ArrayList<>(list.size()), (result, value) -> { result.addAll(value); return result; });
    }

    @Override
    public <T, U> List<U> map(int i, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        return execute(i, list, (source) -> {
            List<U> results = new LinkedList<>();

            for (T value : source) results.add(function.apply(value));

            return results;
        }).stream().reduce(new ArrayList<>(list.size()), (result, value) -> { result.addAll(value);return result; });
    }

    private static class Aggregator<T> {
        T value = null;
        int count = 0;
    }

    protected  <T, R> List<R> execute(int n, List<? extends T> data, Function<List<T>, ? extends R> function) throws InterruptedException {
        return new Executor<T,R>(n, data, function).execute();
    }

    private static class Executor<T, R> {
        private List<Thread> threads;
        private List<AsyncTask> tasks;

        protected class AsyncTask implements Runnable {
            private R result;
            private Function<List<T>, ? extends R> function;
            private List<? extends T> data;

            AsyncTask(Function<List<T>, ? extends R> f, List<? extends T> data) {
                this.function = f;
                this.data = data;
            }

            @Override
            @SuppressWarnings("unchecked")
            public void run() {
                result = function.apply((List<T>) data);
            }
        }

        Executor(int n, List<? extends T> data, Function<List<T>, ? extends R> function) {
            List<List<? extends T>> parts = split(data, n);
            threads = new ArrayList<>(parts.size());
            tasks   = new ArrayList<>(parts.size());

            for (List<? extends T> part: parts) {
                AsyncTask task = new AsyncTask(function, part);

                tasks.add(task);
                threads.add(new Thread(task));
            }
        }

        List<R> execute() throws InterruptedException {
            // Spawn
            for (Thread t: threads) {
                t.start();
            }
            // Wait
            for (Thread t: threads) {
                t.join();
            }
            // Collect
            List<R> results = new ArrayList<>(threads.size());
            for (AsyncTask task: tasks) {
                results.add(task.result);
            }
            return results;
        }

    }

    protected static <T> List<List<? extends T>> split(List<? extends T> data, int n) {
        int chunk = (int) Math.ceil(data.size() / (n * 1.0));
        List<List<? extends T>> result = new ArrayList<>(data.size() / chunk);

        for (int base = 0, end; base < data.size(); base = end) {
            end = Math.min(data.size(), base + chunk);
            result.add(data.subList(base, end));
        }

        return result;
    }
}

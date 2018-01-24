package ru.ifmo.ctddev.solutions.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class IterativeParallelism implements ListIP {

    private <T, R> List<R> execute(int n, List<? extends T> data, ExtendedFunction<T, R> function) throws InterruptedException {
        ThreadWorker<T, R> worker = new ThreadWorker<>(n, data, function);
        return worker.execute();
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        ExtendedFunction<T, T> function = t -> t.stream()
                .max(comparator)
                .orElseThrow(() -> new RuntimeException("No maximum in array"));
        List<T> result = execute(threads, values, function);
        T max = null;
        for (T resultValue : result) {
            if (resultValue != null) {
                if (max == null) {
                    max = resultValue;
                } else {
                    max = comparator.compare(max, resultValue) < 0 ? resultValue : max;
                }
            }
        }
        return max;
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        ExtendedFunction<T, T> function = t -> t.stream()
                .min(comparator)
                .orElseThrow(() -> new RuntimeException("No minimum in array."));
        List<T> result = execute(threads, values, function);
        T min = null;
        for (T resultValue : result) {
            if (resultValue != null) {
                if (min == null) {
                    min = resultValue;
                } else {
                    min = comparator.compare(min, resultValue) > 0 ? resultValue : min;
                }
            }
        }
        return min;
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        ExtendedFunction<T, Boolean> function = l -> {
            for (T element : l) {
                if (Thread.currentThread().isInterrupted()) {
                    return null;
                }
                if (!predicate.test(element)) {
                    return false;
                }
            }
            return true;
        };
        List<Boolean> result = execute(threads, values, function);
        return result.stream().filter(Objects::nonNull)
                .reduce(true, (s, c) -> s && c);
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        ExtendedFunction<T, Boolean> function = l -> {
            for (T element : l) {
                if (Thread.currentThread().isInterrupted()) {
                    return null;
                }
                if (predicate.test(element)) {
                    return true;
                }
            }
            return false;
        };
        List<Boolean> result = execute(threads, values, function);
        return result.stream()
                .filter(Objects::nonNull)
                .reduce(false, (s, c) -> s || c);
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        List<String> result = execute(threads, values, (s) -> {
            StringBuilder sb = new StringBuilder();
            s.forEach(f -> {
                if (f != null) {
                    sb.append(f);
                }
            });
            return sb.toString();
        });
        StringBuilder output = new StringBuilder();
        for (String s : result) {
            if (s != null) {
                output.append(s);
            }
        }
        return output.toString();
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        ExtendedFunction<T, List<T>> function = l -> l.stream()
                .filter(predicate)
                .collect(toList());
        List<List<T>> result = execute(threads, values, function);
        List<T> output = new ArrayList<>();
        for (List<T> list : result) {
            if (list != null) {
                output.addAll(list);
            }
        }
        return output;
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        ExtendedFunction<T, List<U>> function = l -> l.stream()
                .map(f)
                .collect(toList());
        List<List<U>> result = execute(threads, values, function);
        List<U> output = new ArrayList<>();
        for (List<U> list : result) {
            if (list != null) {
                output.addAll(list);
            }
        }
        return output;
    }

    public static class ThreadWorker<T, R> {
        private List<R> results;
        private Thread[] threads;
        private ExtendedFunction<T, R> functions;
        private List<List<? extends T>> arrays;

        protected class Worker2 implements Runnable {
            private int i;
            private ThreadWorker<T, R> worker;

            Worker2(ThreadWorker<T, R> worker, int i) {
                this.worker = worker;
                this.i = i;
            }

            @Override
            public void run() {
                results.set(i, this.worker.run(i));
            }
        }

        ThreadWorker(int threadNumber, List<? extends T> data, ExtendedFunction<T, R> function) {
            this.functions = function;
            this.results = new ArrayList<>(threadNumber);
            IntStream.range(0, threadNumber).forEach(i -> this.results.add(null));
            arrays = splitToChunks(data, threadNumber);
            threads = new Thread[arrays.size()];
            for (int i = 0; i < arrays.size(); i++) {
                threads[i] = new Thread(new Worker2(this, i));
            }
        }

        R run(int i) {
            return this.functions.apply(arrays.get(i));
        }

        List<R> execute() throws InterruptedException {
            for (Thread t: threads) {
                t.start();
            }
            for (Thread t: threads) {
                t.join();
            }
            return results;
        }
    }

    private static <T> List<List<? extends T>> splitToChunks(List<? extends T> list, int chunksNum) {
        List<List<? extends T>> chunks = new ArrayList<>();
        int chunkSize = list.size() / chunksNum;

        if (chunkSize <= 0) {
            chunksNum = 1;
            chunkSize = list.size();
        }

        for (int i = 0; i < chunksNum; i++) {
            int fromIndex = i * chunkSize;
            int toIndex = chunkSize * (i + 1);
            if (list.size() % chunksNum > 0 && i == chunksNum - 1) {
                chunks.add(list.subList(fromIndex, list.size()));
            } else {
                chunks.add(list.subList(fromIndex, toIndex));
            }
        }

        return chunks;
    }

    interface ExtendedFunction<T, R> extends Function<List<? extends T>, R> {
    }
}

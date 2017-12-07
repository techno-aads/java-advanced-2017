package ru.ifmo.ctddev.solutions.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class IterativeParallelism implements ListIP {

    @Override
    public <T> T maximum(int n, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        if (list.isEmpty()) {
            return null;
        }

        int chunkSize = Math.max((list.size() / n) + 1, 10);
        T[] results = (T[]) new Object[n];
        Thread[] threads = new Thread[n];

        for (int i = 0; i < n; i++) {
            if (chunkSize * i > list.size()) {
                break;
            }
            final List<? extends T> subList = list.subList(chunkSize * i, Math.min(chunkSize * (i + 1), list.size()));
            int threadNumber = i;
            threads[i] = new Thread(() -> {
                results[threadNumber] = subList.get(0);
                for (T e : subList) {
                    if (comparator.compare(e, results[threadNumber]) > 0) {
                        results[threadNumber] = e;
                    }
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            if (thread != null) {
                thread.join();
            }
        }

        T max = results[0];
        for (T e : results) {
            if (e != null && comparator.compare(e, max) > 0) {
                max = e;
            }
        }
        return max;
    }

    @Override
    public <T> T minimum(int n, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(n, list, comparator.reversed());
    }

    @Override
    public <T> boolean all(int n, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {

        if (list.isEmpty()) {
            return false;
        }

        int chunkSize = Math.max((list.size() / n) + 1, 10);
        Boolean[] results = new Boolean[n];
        Thread[] threads = new Thread[n];

        for (int i = 0; i < n; i++) {
            if (chunkSize * i > list.size()) {
                break;
            }
            final List<? extends T> subList = list.subList(chunkSize * i, Math.min(chunkSize * (i + 1), list.size()));
            int threadNumber = i;
            threads[i] = new Thread(() -> {
                results[threadNumber] = true;
                for (T e : subList) {
                    if (!predicate.test(e)) {
                        results[threadNumber] = false;
                        break;
                    }
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            if (thread != null) {
                thread.join();
            }
        }

        for (Boolean e : results) {
            if (e != null && !e) {
                return false;
            }
        }
        return true;

    }

    @Override
    public <T> boolean any(int n, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        if (list.isEmpty()) {
            return false;
        }

        int chunkSize = Math.max((list.size() / n) + 1, 10);
        Boolean[] results = new Boolean[n];
        Thread[] threads = new Thread[n];

        for (int i = 0; i < n; i++) {
            if (chunkSize * i > list.size()) {
                break;
            }
            final List<? extends T> subList = list.subList(chunkSize * i, Math.min(chunkSize * (i + 1), list.size()));
            int threadNumber = i;
            threads[i] = new Thread(() -> {
                results[threadNumber] = false;
                for (T e : subList) {
                    if (predicate.test(e)) {
                        results[threadNumber] = true;
                        break;
                    }
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            if (thread != null) {
                thread.join();
            }
        }

        for (Boolean e : results) {
            if (e != null && e) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String join(int n, List<?> list) throws InterruptedException {
        if (list.isEmpty()) {
            return null;
        }

        int chunkSize = Math.max((list.size() / n) + 1, 10);
        StringBuilder[] results = new StringBuilder[n];
        Thread[] threads = new Thread[n];

        for (int i = 0; i < n; i++) {
            if (chunkSize * i > list.size()) {
                break;
            }
            final List<?> subList = list.subList(chunkSize * i, Math.min(chunkSize * (i + 1), list.size()));
            int threadNumber = i;
            threads[i] = new Thread(() -> {
                results[threadNumber] = new StringBuilder();
                for (Object e : subList) {
                    results[threadNumber].append(e);
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            if (thread != null) {
                thread.join();
            }
        }

        StringBuilder result = new StringBuilder();
        for (StringBuilder sb : results) {
            if (sb != null) {
                result.append(sb.toString());
            }
        }
        return result.toString();
    }

    @Override
    public <T> List<T> filter(int n, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        int chunkSize = Math.max((list.size() / n) + 1, 10);
        List<T>[] results = new List[n];
        Thread[] threads = new Thread[n];

        for (int i = 0; i < n; i++) {
            if (chunkSize * i > list.size()) {
                break;
            }
            final List<? extends T> subList = list.subList(chunkSize * i, Math.min(chunkSize * (i + 1), list.size()));
            int threadNumber = i;
            threads[i] = new Thread(() -> results[threadNumber] = subList.stream().filter(predicate).collect(Collectors.toList()));
            threads[i].start();
        }

        for (Thread thread : threads) {
            if (thread != null) {
                thread.join();
            }
        }

        return Arrays.stream(results).filter(Objects::nonNull).flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public <T, U> List<U> map(int n, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        int chunkSize = Math.max((list.size() / n) + 1, 10);
        List<U>[] results = new List[n];
        Thread[] threads = new Thread[n];

        for (int i = 0; i < n; i++) {
            if (chunkSize * i > list.size()) {
                break;
            }
            final List<? extends T> subList = list.subList(chunkSize * i, Math.min(chunkSize * (i + 1), list.size()));
            int threadNumber = i;
            threads[i] = new Thread(() -> results[threadNumber] = subList.stream().map(function).collect(Collectors.toList()));
            threads[i].start();
        }

        for (Thread thread : threads) {
            if (thread != null) {
                thread.join();
            }
        }

        return Arrays.stream(results).filter(Objects::nonNull).flatMap(Collection::stream).collect(Collectors.toList());
    }
}

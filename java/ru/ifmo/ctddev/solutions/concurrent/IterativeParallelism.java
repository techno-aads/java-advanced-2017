package ru.ifmo.ctddev.solutions.concurrent;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

public class IterativeParallelism implements ListIP {

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        Thread[] threadPool = new Thread[threads];
        List<T> maximums = new ArrayList<>();
        int countThread = Integer.min(threads, values.size());
        int sizeSubList = values.size() / countThread;

        for (int i = 0; i < countThread; i++) {
            maximums.add(null);
        }

        for (int i = 0; i < countThread; i++) {
            int localI = i;
            Thread thread = new Thread(() -> {
                int startIndex = sizeSubList * localI;
                int endIndex = (localI == countThread - 1) ? values.size() : startIndex + sizeSubList;
                T localMax = Collections.max(values.subList(startIndex, endIndex), comparator);
                maximums.set(localI, localMax);
            });

            threadPool[i] = thread;
            thread.start();
        }

        for (int i = 0; i < countThread; i++) {
            threadPool[i].join();
        }

        return Collections.max(maximums, comparator);
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, Collections.reverseOrder(comparator));
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        Thread[] threadPool = new Thread[threads];
        int countThread = Integer.min(threads, values.size());
        int sizeSubList = values.size() / countThread;
        List<Boolean> result = new ArrayList<>();
        Object locker = new Object();

        result.add(true);


        for (int i = 0; i < countThread; i++) {
            int localI = i;
            Thread thread = new Thread(() -> {
                int startIndex = sizeSubList * localI;
                int endIndex = (localI == countThread - 1) ? values.size() : startIndex + sizeSubList;

                if (startIndex < endIndex) {
                    boolean localResult = values.subList(startIndex, endIndex).stream().allMatch(predicate);
                    synchronized (locker) {
                        if (!localResult) {
                            result.set(0, false);
                        }
                    }
                }
            });

            threadPool[i] = thread;
            thread.start();
        }

        for (int i = 0; i < countThread; i++) {
            threadPool[i].join();
        }

        return result.get(0);
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        Thread[] threadPool = new Thread[threads];
        List<String> allString = new ArrayList<>(threads);
        int countThread = Integer.min(threads, values.size());
        int sizeSubList = values.size() / countThread;
        for (int i = 0; i < countThread; i++) {
            allString.add(null);
        }

        for (int i = 0; i < countThread; i++) {
            int localI = i;
            Thread thread = new Thread(() -> {
                int startIndex = sizeSubList * localI;
                int endIndex = (localI == countThread - 1) ? values.size() : startIndex + sizeSubList;
                String localString = values.subList(startIndex, endIndex).stream().map(Object::toString).collect(Collectors.joining());
                allString.set(localI, localString);
            });

            threadPool[i] = thread;
            thread.start();
        }

        for (int i = 0; i < countThread; i++) {
            threadPool[i].join();
        }

        return String.join("", allString);
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        Thread[] threadPool = new Thread[threads];

        int countThread = Integer.min(threads, values.size());
        int sizeSubList = values.size() / countThread;
        Map<Integer, List<T>> result = new HashMap<>();

        for (int i = 0; i < countThread; i++) {
            result.put(i, null);
        }

        for (int i = 0; i < countThread; i++) {
            int localI = i;
            Thread thread = new Thread(() -> {
                int startIndex = sizeSubList * localI;
                int endIndex = (localI == countThread - 1) ? values.size() : startIndex + sizeSubList;

                if (startIndex < endIndex) {
                    List<T> filterList = values.subList(startIndex, endIndex).stream().filter(predicate).collect(Collectors.toList()); //filter(list.subList(startIndex, endIndex), predicate);
                    result.put(localI, filterList);
                }
            });

            threadPool[i] = thread;
            thread.start();
        }

        for (int i = 0; i < countThread; i++) {
            threadPool[i].join();
        }

        List<T> filterList = new ArrayList<>();

        for (int i = 0; i < countThread; i++) {
            filterList.addAll(result.get(i));
        }

        return filterList;
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        Thread[] threadPool = new Thread[threads];

        int countThread = Integer.min(threads, values.size());
        int sizeSubList = values.size() / countThread;
        Map<Integer, List<U>> result = new HashMap<>();

        for (int i = 0; i < countThread; i++) {
            result.put(i, null);
        }

        for (int i = 0; i < countThread; i++) {
            int localI = i;
            Thread thread = new Thread(() -> {
                int startIndex = sizeSubList * localI;
                int endIndex = (localI == countThread - 1) ? values.size() : startIndex + sizeSubList;
                if (startIndex < endIndex) {
                    List<U> filterList = values.subList(startIndex, endIndex).stream().map(f).collect(Collectors.toList()); //map(list.subList(startIndex, endIndex), f);
                    result.put(localI, filterList);
                }
            });

            threadPool[i] = thread;
            thread.start();
        }

        for (int i = 0; i < countThread; i++) {
            threadPool[i].join();
        }

        List<U> filterList = new ArrayList<>();

        for (int i = 0; i < countThread; i++) {
            filterList.addAll(result.get(i));
        }

        return filterList;
    }
}

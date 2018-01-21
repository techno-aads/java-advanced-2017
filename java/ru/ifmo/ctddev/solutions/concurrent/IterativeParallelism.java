package ru.ifmo.ctddev.solutions.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class IterativeParallelism implements ListIP {

    @Override
    public String join(int i, List<?> list) throws InterruptedException {
        Function<List<?>, String> func = l -> l.stream()
                .map(Object::toString)
                .collect(Collectors.joining(""));
        List<String> strings = func(i, list, func);

        return strings.stream()
                .map(Object::toString)
                .collect(Collectors.joining(""));
    }

    @Override
    public <T> List<T> filter(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        List<T> resultList = new ArrayList<>();
        Function<List<? extends T>, List<T>> func = l -> l.stream().filter(predicate).collect(Collectors.toList());
        List<List<T>> lists = func(i, list, func);
        for (List<T> listU : lists) {
            resultList.addAll(listU);
        }
        return resultList;
    }

    @Override
    public <T, U> List<U> map(int i, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        List<U> resultList = new ArrayList<>();
        Function<List<? extends T>, List<U>> func = l -> l.stream().map(s -> function.apply(s)).collect(Collectors.toList());
        List<List<U>> lists = func(i, list, func);
        for (List<U> listU : lists) {
            resultList.addAll(listU);
        }
        return resultList;
    }

    @Override
    public <T> T maximum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        Function<List<? extends T>, T> func = l -> Collections.max(l, comparator);
        List<T> listMax = func(i, list, func);
        return Collections.max(listMax, comparator);
    }

    @Override
    public <T> T minimum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        Function<List<? extends T>, T> func = l -> Collections.min(l, comparator);
        return Collections.min(func(i, list, func), comparator);
    }

    @Override
    public <T> boolean all(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        Function<List<? extends T>, Boolean> func = l -> l.stream().allMatch(predicate);
        List<Boolean> listBoolean = func(i, list, func);
        for (Boolean b : listBoolean) {
            if (!b) return false;
        }
        return true;
    }

    @Override
    public <T> boolean any(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        Function<List<? extends T>, Boolean> func = l -> l.stream().anyMatch(predicate);
        List<Boolean> listBoolean = func(i, list, func);
        for (Boolean b : listBoolean) {
            if (b) return true;
        }
        return false;
    }

    private <T, R> List<R> func(int threadCounter, List<? extends T> list, Function<List<? extends  T>, R> f) throws InterruptedException {
        if (threadCounter > list.size()) {
            threadCounter = list.size() / 5 + 1;
        }
        R[] results = (R[]) new Object[threadCounter];

        List<Thread> threads = new ArrayList<>();

        int subListSize = list.size() / threadCounter;
        for (int i = 0; i < threadCounter; i++) {
            int threadCount = threadCounter;
            int threadIndex = i;

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (threadCount - 1 != threadIndex) {
                        results[threadIndex] = f.apply(list.subList(subListSize * threadIndex, subListSize * (threadIndex + 1)));
                    } else {
                        results[threadIndex] = f.apply(list.subList(subListSize * threadIndex, list.size()));
                    }
                }
            });
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        return Arrays.asList(results);
    }
}


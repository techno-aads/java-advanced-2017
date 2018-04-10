package ru.ifmo.ctddev.solutions.concurrent;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

/**
 * @author Sergey Egorov
 */
public class IterativeParallelism implements ListIP {

    private <T, R> List<R> executeParallel(int countThreads, List<? extends T> list, Function<List<? extends T>, R> funcName) throws InterruptedException {
        if (countThreads > list.size()) {
            countThreads = list.size() / 5 + 1;
        }
        R[] results = (R[]) new Object[countThreads];

        List<Thread> threads = new ArrayList<>();

        int subListSize = list.size() / countThreads;
        for (int i = 0; i < countThreads; i++) {
            int threadCount = countThreads;
            int threadIndex = i;

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (threadCount - 1 != threadIndex) {
                        results[threadIndex] = funcName.apply(list.subList(subListSize * threadIndex, subListSize * (threadIndex + 1)));
                    } else {
                        results[threadIndex] = funcName.apply(list.subList(subListSize * threadIndex, list.size()));
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

    @Override
    public <T> T maximum(int countThread, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        Function<List<? extends T>, T> func = l -> Collections.max(l, comparator);
        List<T> listMax = executeParallel(countThread, list, func);
        return Collections.max(listMax, comparator);
    }

    @Override
    public <T> T minimum(int countThread, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        Function<List<? extends T>, T> func = l -> Collections.min(l, comparator);
        return Collections.min(executeParallel(countThread, list, func), comparator);
    }

    @Override
    public <T> boolean all(int countThread, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        Function<List<? extends T>, Boolean> func = l -> l.stream().allMatch(predicate);
        List<Boolean> listBoolean = executeParallel(countThread, list, func);
        for (Boolean b : listBoolean) {
            if (!b) return false;
        }
        return true;
    }

    @Override
    public <T> boolean any(int countThread, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        Function<List<? extends T>, Boolean> func = l -> l.stream().anyMatch(predicate);
        List<Boolean> listBoolean = executeParallel(countThread, list, func);
        for (Boolean b : listBoolean) {
            if (b) return true;
        }
        return false;
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        throw new UnsupportedOperationException();
    }
}

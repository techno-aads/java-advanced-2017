package ru.ifmo.ctddev.solutions.concurrent;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

public class IterativeParallelism implements ListIP {

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return parallelListProcessing(
                threads, values,
                subList -> subList.max(comparator).get(),
                lists -> lists.max(comparator).get()
        );
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return parallelListProcessing(
                threads, values,
                subList -> subList.min(comparator).get(),
                lists -> lists.min(comparator).get()
        );
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return parallelListProcessing(
                threads, values,
                subList -> subList.allMatch(predicate),
                lists -> lists.reduce((o1, o2) -> (o1 && o2)).get()
        );
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return parallelListProcessing(
                threads, values,
                subList -> subList.anyMatch(predicate),
                lists -> lists.reduce((o1, o2) -> (o1 || o2)).get()
        );
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return parallelListProcessing(
                threads, values,
                subList -> subList.map(Object::toString).reduce(String::concat).get(),
                lists -> lists.reduce(String::concat).get()
        );
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return parallelListProcessing(
                threads, values,
                subList -> subList.filter(predicate).collect(Collectors.toList()),
                lists -> lists.flatMap(List::stream).collect(Collectors.toList())
        );
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> function) throws InterruptedException {
        return parallelListProcessing(
                threads, values,
                subList -> subList.map(function).collect(Collectors.toList()),
                lists -> lists.flatMap(List::stream).collect(Collectors.toList())
        );
    }

    private <T, M, R> R parallelListProcessing(
            int count,
            List<T> list,
            Function<Stream<T>, M> threadFunction,
            Function<Stream<M>, R> resultFunction
    ) throws InterruptedException {
        count = Math.min(count, list.size());
        M[] results = (M[]) new Object[count];
        Thread[] threads = new Thread[count];
        int step = list.size() / count;
        int ost = list.size() % count;

        for (int i = 0, j = 0; j < list.size(); i++) {
            int from = j;
            int to = j + step;
            if (ost != 0) {
                ost--;
                to++;
            }
            j = to;
            int fI = i;
            int fTo = to;
            threads[i] = new Thread(() -> results[fI] = threadFunction.apply(list.subList(from, fTo).stream()));
            threads[i].start();
        }
        for (int i = 0; i < count; i++) {
            threads[i].join();
        }
        return resultFunction.apply(Arrays.stream(results));
    }
}

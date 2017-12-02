package ru.ifmo.ctddev.solutions.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class IterativeParallelismWithMapper implements ListIP {

    private final ParallelMapper mapper;

    public IterativeParallelismWithMapper(ParallelMapper parallelMapper) {
        this.mapper = parallelMapper;
    }

    @Override
    public <T> T maximum(int n, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return executeInParallel(n, list, subList -> Collections.max(subList, comparator), results -> Collections.max(results, comparator));
    }

    @Override
    public <T> T minimum(int n, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return executeInParallel(n, list, subList -> Collections.min(subList, comparator), results -> Collections.min(results, comparator));
    }

    @Override
    public <T> boolean all(int n, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return executeInParallel(n, list, subList -> subList.stream().allMatch(predicate), results -> results.stream().allMatch(Predicate.isEqual(true)));
    }

    @Override
    public <T> boolean any(int n, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return executeInParallel(n, list, subList -> subList.stream().anyMatch(predicate), results -> results.stream().anyMatch(Predicate.isEqual(true)));
    }

    @Override
    public String join(int n, List<?> list) throws InterruptedException {
        return executeInParallel(n, list, subList -> subList.stream().map(Object::toString).collect(Collectors.joining()),
                results -> results.stream().map(Object::toString).collect(Collectors.joining()));
    }

    @Override
    public <T> List<T> filter(int n, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return executeInParallel(n, list,
                subList -> subList.stream().filter(predicate).collect(Collectors.toList()),
                results -> results.stream().reduce(new ArrayList<T>(), (x, y) -> {
                    x.addAll(y);
                    return x;
                }));
    }

    @Override
    public <T, U> List<U> map(int n, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        return executeInParallel(n, list,
                subList -> subList.stream().map(function).collect(Collectors.toList()),
                results -> results.stream().reduce(new ArrayList<U>(), (x, y) -> {
                    x.addAll(y);
                    return x;
                }));
    }

    private <T> List<List<? extends T>> getParts(int threads, List<? extends T> list) {
        int chunkSize = Math.max((list.size() / threads) + 1, 10);
        List<List<? extends T>> result = new ArrayList<>(list.size() / chunkSize + 1);
        for (int i = 0; i < threads; i++) {
            if (chunkSize * i > list.size()) {
                break;
            }
            result.add(list.subList(chunkSize * i, Math.min(chunkSize * (i + 1), list.size())));
        }
        return result;
    }

    private <T, R> R executeInParallel(int threads, List<? extends T> list,
                                       Function<List<? extends T>, R> action,
                                       Function<List<R>, R> combiner) throws InterruptedException {
        return combiner.apply(mapper.map(action, getParts(threads, list)));
    }
}

package ru.ifmo.ctddev.solutions.mapper;


import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

/**
 * @author Sergey Egorov
 */
public class IterativeParallelism implements ListIP {

    private ParallelMapper parallelMapper;

    public IterativeParallelism(ParallelMapper parallelMapper) {
        this.parallelMapper = parallelMapper;
    }

    private <T> List<List<? extends T>> submitList(int threadCounter, List<? extends T> list) throws InterruptedException {
        if (threadCounter > list.size()) {
            threadCounter = list.size() / 5 + 1;
        }

        List<List<? extends T>> results = new ArrayList<>();

        int subListSize = list.size() / threadCounter;
        for (int i = 0; i < threadCounter; i++) {
            int threadCount = threadCounter;
            int threadIndex = i;

            if (threadCount - 1 != threadIndex) {
                results.add(list.subList(subListSize * threadIndex, subListSize * (threadIndex + 1)));
            } else {
                results.add(list.subList(subListSize * threadIndex, list.size()));
            }
        }

        return results;
    }

    @Override
    public <T> T maximum(int countThread, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        Function<List<? extends T>, T> func = l -> Collections.max(l, comparator);
        List<List<? extends T>> listListMax = submitList(countThread, list);
        return Collections.max(parallelMapper.map(func, listListMax), comparator);
    }

    @Override
    public <T> T minimum(int countThread, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        Function<List<? extends T>, T> func = l -> Collections.max(l, comparator);
        List<List<? extends T>> listListMin = submitList(countThread, list);
        return Collections.min(parallelMapper.map(func, listListMin), comparator);
    }

    @Override
    public <T> boolean all(int countThread, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        Function<List<? extends T>, Boolean> func = l -> l.stream().allMatch(predicate);
        List<List<? extends T>> listListBoolean = submitList(countThread, list);
        List<Boolean> listBoolean = parallelMapper.map(func, listListBoolean);
        for (Boolean b : listBoolean) {
            if (!b) return false;
        }
        return true;
    }

    @Override
    public <T> boolean any(int countThread, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        Function<List<? extends T>, Boolean> func = l -> l.stream().anyMatch(predicate);
        List<List<? extends T>> listListBoolean = submitList(countThread, list);
        List<Boolean> listBoolean = parallelMapper.map(func, listListBoolean);
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


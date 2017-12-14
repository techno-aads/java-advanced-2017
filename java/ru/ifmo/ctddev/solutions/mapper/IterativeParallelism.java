package ru.ifmo.ctddev.solutions.mapper;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class IterativeParallelism implements ListIP {

    private ParallelMapper parallelMapper;

    public IterativeParallelism(ParallelMapper parallelMapper) {
        this.parallelMapper = parallelMapper;
    }

    @Override
    public String join(int i, List<?> list) throws InterruptedException {
        Function<List<?>, String> func = l -> l.stream()
                .map(Object::toString)
                .collect(Collectors.joining(""));
        List<List<?>> listList = subLister(i, list);
        List<String> strings = parallelMapper.map(func, listList);
        return strings.stream()
                .map(Object::toString)
                .collect(Collectors.joining(""));
    }

    @Override
    public <T> List<T> filter(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        List<T> resultList = new ArrayList<>();
        Function<List<? extends T>, List<T>> func = l -> l.stream().filter(predicate).collect(Collectors.toList());

        List<List<? extends T>> listList = subLister(i, list);
        List<List<T>> lists = parallelMapper.map(func, listList);
        for (List<T> listU : lists) {
            resultList.addAll(listU);
        }
        return resultList;
    }

    @Override
    public <T, U> List<U> map(int i, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        List<U> resultList = new ArrayList<>();
        Function<List<? extends T>, List<U>> func = l -> l.stream().map(s -> function.apply(s)).collect(Collectors.toList());
        List<List<? extends T>> listList = subLister(i, list);
        List<List<U>> lists = parallelMapper.map(func, listList);
        for (List<U> listU : lists) {
            resultList.addAll(listU);
        }
        return resultList;
    }

    @Override
    public <T> T maximum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        Function<List<? extends T>, T> func = l -> Collections.max(l, comparator);
        List<List<? extends T>> listList = subLister(i, list);

        return Collections.max(parallelMapper.map(func, listList), comparator);
    }

    @Override
    public <T> T minimum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        Function<List<? extends T>, T> func = l -> Collections.min(l, comparator);
        List<List<? extends T>> listList = subLister(i, list);

        return Collections.min(parallelMapper.map(func, listList), comparator);
    }

    @Override
    public <T> boolean all(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        Function<List<? extends T>, Boolean> func = l -> l.stream().allMatch(predicate);
        List<List<? extends T>> listList = subLister(i, list);
        List<Boolean> listBoolean = parallelMapper.map(func, listList);
        for (Boolean b : listBoolean) {
            if (!b) return false;
        }
        return true;
    }

    @Override
    public <T> boolean any(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        Function<List<? extends T>, Boolean> func = l -> l.stream().anyMatch(predicate);
        List<List<? extends T>> listList = subLister(i, list);
        List<Boolean> listBoolean = parallelMapper.map(func, listList);
        for (Boolean b : listBoolean) {
            if (b) return true;
        }
        return false;
    }

    private <T> List<List<? extends T>> subLister(int threadCounter, List<? extends T> list) throws InterruptedException {
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
}

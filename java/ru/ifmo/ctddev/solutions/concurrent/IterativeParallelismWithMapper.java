package ru.ifmo.ctddev.solutions.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class IterativeParallelismWithMapper implements ListIP {

    private final ParallelMapper mapper;

    public IterativeParallelismWithMapper(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    public <T> T maximum(int threadsLimit, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        int taskNum = getTaskNumber(threadsLimit, list);

        Function<List<? extends T>, T> listMaxFunction = (l -> Collections.max(l, comparator));

        List<List<? extends T>> mapperArguments = prepareMapperArguments(list, taskNum);
        List<T> mapResult = mapper.map(listMaxFunction, mapperArguments);

        return Collections.max(mapResult, comparator);
    }

    public <T> T minimum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        int taskNum = getTaskNumber(i, list);

        Function<List<? extends T>, T> f = l -> Collections.min(l, comparator);

        List<List<? extends T>> mapperArguments = prepareMapperArguments(list, taskNum);
        List<T> mapResult = mapper.map(f, mapperArguments);

        return Collections.min(mapResult, comparator);
    }

    public <T> boolean all(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        int taskNum = getTaskNumber(i, list);

        Function<List<? extends T>, Boolean> f = l -> l.stream().allMatch(predicate);

        List<List<? extends T>> mapperArguments = prepareMapperArguments(list, taskNum);
        List<Boolean> mapResult = mapper.map(f, mapperArguments);

        return mapResult.stream().allMatch(value -> value);

    }

    public <T> boolean any(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        int taskNum = getTaskNumber(i, list);

        Function<List<? extends T>, Boolean> f = l -> l.stream().anyMatch(predicate);

        List<List<? extends T>> mapperArguments = prepareMapperArguments(list, taskNum);
        List<Boolean> mapResult = mapper.map(f, mapperArguments);

        return mapResult.stream().anyMatch(value -> value);
    }

    @Override
    public String join(int i, List<?> list) throws InterruptedException {
        int taskNum = getTaskNumber(i, list);

        Function<List<?>, String> f = l -> l.stream().map(Object::toString).collect(Collectors.joining());

        List<List<?>> mapperArguments = prepareMapperArguments(list, taskNum);
        List<String> mapResult = mapper.map(f, mapperArguments);

        return mapResult.stream().collect(Collectors.joining());
    }

    @Override
    public <T> List<T> filter(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        int taskNum = getTaskNumber(i, list);

        Function<List<? extends T>, List<T>> f = l -> l.stream().filter(predicate).collect(Collectors.toList());

        List<List<? extends T>> mapperArguments = prepareMapperArguments(list, taskNum);
        List<List<T>> mapResult = mapper.map(f, mapperArguments);

        return mapResult.stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public <T, U> List<U> map(int i, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        int taskNum = getTaskNumber(i, list);
        List<List<U>> threadResults = new ArrayList<>(Collections.nCopies(taskNum, null));

        Function<List<? extends T>, List<U>> f = l -> l.stream().map(function).collect(Collectors.toList());

        List<List<? extends T>> mapperArguments = prepareMapperArguments(list, taskNum);
        List<List<U>> mapResult = mapper.map(f, mapperArguments);

        return mapResult.stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    private <T> int getTaskNumber(int threadasLimit, List<? extends T> list) {
        return list.size() < threadasLimit ? list.size() : threadasLimit;
    }

    private <T> List<List<? extends T>> prepareMapperArguments(List<? extends T> list, int taskNum) {
        List<List<? extends T>> listArrayList = new ArrayList<>(taskNum);

        int subListLength = list.size() / taskNum;
        for (int task = 0; task < taskNum; task++) {
            int fromIndex = subListLength * task;
            int toIndex = task == taskNum - 1 ? list.size() : fromIndex + subListLength;

            listArrayList.add(list.subList(fromIndex, toIndex));
        }
        return listArrayList;
    }
}
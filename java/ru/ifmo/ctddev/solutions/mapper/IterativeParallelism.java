package ru.ifmo.ctddev.solutions.mapper;

import com.sun.org.apache.xpath.internal.operations.Bool;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class IterativeParallelism implements ListIP {

    ParallelMapper parallelMapper;

    public IterativeParallelism (ParallelMapper parallelMapper){
        this.parallelMapper = parallelMapper;
    }

    private<T> T  max (List<? extends T> x){
        return null;
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        Function<List<? extends T>, T> max = l -> l.stream().max(comparator).orElse(null);
        List<List<? extends T>> subLists = generateSubLists(threads, values);
        return Collections.max(parallelMapper.map(max, subLists), comparator);
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, Collections.reverseOrder(comparator));
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        Function<List<? extends T>, Boolean> allFunc = l -> l.stream().allMatch(predicate);
        List<List<? extends T>> subLists = generateSubLists(threads, values);
        List<Boolean> results = parallelMapper.map(allFunc, subLists);

        for (Boolean result : results) {
            if (!result){
                return false;
            }
        }

        return true;
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        List<String> allString;
        Function<List<?>, String> joinFunc = l -> l.stream().map(Object::toString).collect(Collectors.joining());
        allString = parallelMapper.map(joinFunc, generateSubLists(threads, values));
        return String.join("", allString);
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        Function<List<? extends T>, List<T>> filterFunc = l -> l.stream().filter(predicate).collect(Collectors.toList());
        List<List<? extends T>> subLists = generateSubLists(threads, values);
        List<List<T>> filterList = parallelMapper.map(filterFunc, subLists);

        List<T> result = new ArrayList<>();

        for (List<T> list : filterList) {
            result.addAll(list);
        }

        return result;
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        Function<List<? extends T>, List<U>> mapFunc = l -> l.stream().map(f).collect(Collectors.toList());

        List<List<U>> mapResults = parallelMapper.map(mapFunc, generateSubLists(threads, values));
        List<U> result = new ArrayList<>();

        for (List<U> mapResult : mapResults) {
            result.addAll(mapResult);
        }

        return result;
    }

    private<T> List<List<? extends T>> generateSubLists (int threads, List<? extends T> values){
        int countThread = Math.min(threads, values.size());
        List<List<? extends T>> subLists = new ArrayList<>();
        int startIndex;
        int endIndex;
        int sizeSubList = values.size() / countThread;

        for (int i = 0; i < countThread; i++){
            startIndex = i * sizeSubList;
            endIndex = i == (countThread-1) ? values.size() : (i+1)*sizeSubList;
            subLists.add( values.subList(startIndex, endIndex) );
        }

        return subLists;
    }
}

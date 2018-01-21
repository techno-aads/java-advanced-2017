package ru.ifmo.ctddev.solutions.mapper;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

public class IterativeParallelism implements ListIP {

    ParallelMapper m_parallelMapper;

    public IterativeParallelism(ParallelMapper parallelMapper){
        m_parallelMapper = parallelMapper;
    }


    @Override
    public String join(int threadsCount, List<?> values) throws InterruptedException {
        List<String> allString;
        Function<List<?>, String> joinFunc = func -> func.stream().map(Object::toString).collect(Collectors.joining());
        allString = m_parallelMapper.map(joinFunc, getTasks(threadsCount, values));
        return String.join("", allString);
    }


    @Override
    public <T> List<T> filter(int threadsCount, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        Function<List<? extends T>, List<T>> filterFunc = func -> func.stream().filter(predicate).collect(Collectors.toList());
        List<List<? extends T>> subLists = getTasks(threadsCount, values);
        List<List<T>> filterList = m_parallelMapper.map(filterFunc, subLists);

        List<T> result = new ArrayList<>();

        for (List<T> list : filterList) {
            result.addAll(list);
        }

        return result;
    }

    @Override
    public <T, U> List<U> map(int threadsCount, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        Function<List<? extends T>, List<U>> mapFunc = func -> func.stream().map(f).collect(Collectors.toList());

        List<List<U>> mapResults = m_parallelMapper.map(mapFunc, getTasks(threadsCount, values));
        List<U> result = new ArrayList<>();

        for (List<U> mapResult : mapResults) {
            result.addAll(mapResult);
        }

        return result;
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        Function<List<? extends T>, T> max = func -> func.stream().max(comparator).orElse(null);
        List<List<? extends T>> subLists = getTasks(threads, values);
        return Collections.max(m_parallelMapper.map(max, subLists), comparator);
    }

    @Override
    public <T> T minimum(int threadsCount, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threadsCount, values, Collections.reverseOrder(comparator));
    }

    @Override
    public <T> boolean all(int threadsCount, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        Function<List<? extends T>, Boolean> allFunc = func -> func.stream().allMatch(predicate);
        List<List<? extends T>> subLists = getTasks(threadsCount, values);
        List<Boolean> results = m_parallelMapper.map(allFunc, subLists);

        for (Boolean result : results) {
            if (!result){
                return false;
            }
        }

        return true;
    }

    @Override
    public <T> boolean any(int threadsCount, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !all(threadsCount, values, predicate.negate());
    }

    private<T> List<List<? extends T>> getTasks (int threadsCount, List<? extends T> values){

        int countThread = Math.min(threadsCount, values.size());
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
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
import java.util.stream.IntStream;

public class IterativeParallelism implements ListIP {

    ParallelMapper m_parallelMapper = null;

    public IterativeParallelism() {}

    public IterativeParallelism(ParallelMapper parallelMapper) {

        if (parallelMapper != null){
            m_parallelMapper = parallelMapper;
        }
    }


    @Override
    public String join(int threadsCount, List<?> values) throws InterruptedException {
        if (m_parallelMapper != null) {
            List<String> allString;
            Function<List<?>, String> joinFunc = func -> func.stream().map(Object::toString).collect(Collectors.joining());
            allString = m_parallelMapper.map(joinFunc, getTasks(threadsCount, values));
            return String.join("", allString);
        } else{
            List<Callable<String>> tasks = getTasks(threadsCount, values, currentList -> currentList.stream().map(Object::toString).collect(Collectors.joining()));
            runAll(tasks.stream().map(Thread::new).collect(Collectors.toList()));
            return tasks.stream().map(Callable::getResponse).collect(Collectors.joining());
        }

    }


    @Override
    public <T> List<T> filter(int threadsCount, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {

        if (m_parallelMapper != null) {
            Function<List<? extends T>, List<T>> filterFunc = func -> func.stream().filter(predicate).collect(Collectors.toList());
            List<List<? extends T>> subLists = getTasks(threadsCount, values);
            List<List<T>> filterList = m_parallelMapper.map(filterFunc, subLists);

            List<T> result = new ArrayList<>();

            for (List<T> list : filterList) {
                result.addAll(list);
            }

            return result;
        } else {
            List<Callable<List<? extends T>>> tasks = getTasks(threadsCount, values, currentList -> currentList.stream().filter(predicate).collect(Collectors.toList()));
            runAll(tasks.stream().map(Thread::new).collect(Collectors.toList()));
            return tasks.stream().map(Callable::getResponse).collect(ArrayList::new, List::addAll, List::addAll);

        }

    }

    @Override
    public <T, U> List<U> map(int threadsCount, List<? extends T> values, Function<? super T, ? extends U> function) throws InterruptedException {
        if (m_parallelMapper != null){
            Function<List<? extends T>, List<U>> mapFunc = func -> func.stream().map(function).collect(Collectors.toList());

            List<List<U>> mapResults = m_parallelMapper.map(mapFunc, getTasks(threadsCount, values));
            List<U> result = new ArrayList<>();

            for (List<U> mapResult : mapResults) {
                result.addAll(mapResult);
            }

            return result;
        } else{
            List<Callable<List<? extends U>>> tasks = getTasks(threadsCount, values, currentList -> currentList.stream().map(function).collect(Collectors.toList()));
            runAll(tasks.stream().map(Thread::new).collect(Collectors.toList()));
            return tasks.stream().map(Callable::getResponse).collect(ArrayList::new, List::addAll, List::addAll);
        }

    }

    @Override
    public <T> T maximum(int threadsCount, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        if (m_parallelMapper != null) {
            Function<List<? extends T>, T> max = func -> func.stream().max(comparator).orElse(null);
            List<List<? extends T>> subLists = getTasks(threadsCount, values);
            return Collections.max(m_parallelMapper.map(max, subLists), comparator);
        } else {
            List<Callable<T>> tasks = getTasks(threadsCount, values, currentList -> currentList.stream().max(comparator).get());
            runAll(tasks.stream().map(Thread::new).collect(Collectors.toList()));
            return tasks.stream().map(Callable::getResponse).max(comparator).get();
        }
    }

    @Override
    public <T> T minimum(int threadsCount, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threadsCount, values, Collections.reverseOrder(comparator));
    }

    @Override
    public <T> boolean all(int threadsCount, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        if (m_parallelMapper != null) {
            Function<List<? extends T>, Boolean> allFunc = func -> func.stream().allMatch(predicate);
            List<List<? extends T>> subLists = getTasks(threadsCount, values);
            List<Boolean> results = m_parallelMapper.map(allFunc, subLists);

            for (Boolean result : results) {
                if (!result) {
                    return false;
                }
            }

            return true;
        } else{
            List<Callable<Boolean>> tasks = getTasks(threadsCount, values, currentList -> currentList.stream().allMatch(predicate));
            runAll(tasks.stream().map(Thread::new).collect(Collectors.toList()));
            return tasks.stream().allMatch(Callable::getResponse);
        }
    }

    @Override
    public <T> boolean any(int threadsCount, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !all(threadsCount, values, predicate.negate());
    }

    private<T> List<List<? extends T>> getTasks (int threadsCount, List<? extends T> values){

        int numberofActiveThread = Math.min(threadsCount, values.size());
        List<List<? extends T>> subLists = new ArrayList<>();
        int startIndex;
        int endIndex;
        int sizeSubList = values.size() / numberofActiveThread;

        for (int i = 0; i < numberofActiveThread; i++){
            startIndex = i * sizeSubList;
            endIndex = i == (numberofActiveThread-1) ? values.size() : (i+1)*sizeSubList;
            subLists.add( values.subList(startIndex, endIndex) );
        }

        return subLists;
    }

    private <R, T> List<Callable<R>> getTasks(int countThread, List<T> list, Function<List<T>, R> function) {
        int numberOfElements = countThread > list.size() ? 1 : list.size() / countThread;
        int numberofActiveThread = Math.min(countThread, list.size());
        return IntStream.range(0, numberofActiveThread).boxed().map(currentThread -> {
                    int to = currentThread * numberOfElements;
                    int from = (currentThread == numberofActiveThread - 1) ? list.size() : to + numberOfElements;
                    return new Callable<R>() {
                        @Override
                        public void run() {
                            List<T> currentList = list.subList(to, from);
                            setResponse(function.apply(currentList));
                        }
                    };
                }
        ).collect(Collectors.toList());
    }

    private void runAll(List<Thread> threadList) {
        threadList.forEach(Thread::start);
        threadList.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

}
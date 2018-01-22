package ru.ifmo.ctddev.solutions.mapper;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IterativeParallelism implements ListIP {

    private ParallelMapper parallelMapper;

    public IterativeParallelism() {
    }

    public IterativeParallelism(ParallelMapper parallelMapper) {
        this.parallelMapper = parallelMapper;
    }

    @Override
    public String join(int countThread, List<?> list) throws InterruptedException {
        if (Objects.nonNull(parallelMapper)) {
            List<List<?>> chunks = getChunk(countThread, list);
            return map(currentList -> currentList.stream().map(Object::toString).collect(Collectors.joining()), chunks)
                    .stream()
                    .map(Object::toString)
                    .collect(Collectors.joining());
        } else {
            List<Callable<String>> tasks = getTasks(countThread, list, currentList -> currentList.stream().map(Object::toString).collect(Collectors.joining()));
            runAll(tasks.stream().map(Thread::new).collect(Collectors.toList()));
            return tasks.stream().map(Callable::getResponse).collect(Collectors.joining());
        }
    }

    @Override
    public <T> List<T> filter(int countThread, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        if (Objects.nonNull(parallelMapper)) {
            List<List<? extends T>> chunks = getChunk(countThread, list);
            return map(currentList -> currentList.stream().filter(predicate).collect(Collectors.toList()), chunks)
                    .stream()
                    .flatMap(Collection::stream)
                    .filter(predicate)
                    .collect(Collectors.toList());
        } else {
            List<Callable<List<? extends T>>> tasks = getTasks(countThread, list, currentList -> currentList.stream().filter(predicate).collect(Collectors.toList()));
            runAll(tasks.stream().map(Thread::new).collect(Collectors.toList()));
            return tasks.stream().map(Callable::getResponse).collect(ArrayList::new, List::addAll, List::addAll);
        }
    }

    @Override
    public <T, U> List<U> map(int countThread, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        if (Objects.nonNull(parallelMapper)) {
            List<List<? extends T>> chunks = getChunk(countThread, list);
            return map(currentList -> currentList.stream().map(function).collect(Collectors.toList()), chunks)
                    .stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        } else {
            List<Callable<List<? extends U>>> tasks = getTasks(countThread, list, currentList -> currentList.stream().map(function).collect(Collectors.toList()));
            runAll(tasks.stream().map(Thread::new).collect(Collectors.toList()));
            return tasks.stream().map(Callable::getResponse).collect(ArrayList::new, List::addAll, List::addAll);
        }
    }

    @Override
    public <T> T maximum(int countThread, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        if (Objects.nonNull(parallelMapper)) {
            List<List<? extends T>> chunks = getChunk(countThread, list);
            return map(currentList -> currentList.stream().max(comparator).get(), chunks).stream().max(comparator).get();
        } else {
            List<Callable<T>> tasks = getTasks(countThread, list, currentList -> currentList.stream().max(comparator).get());
            runAll(tasks.stream().map(Thread::new).collect(Collectors.toList()));
            return tasks.stream().map(Callable::getResponse).max(comparator).get();
        }
    }

    @Override
    public <T> T minimum(int countThread, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        if (Objects.nonNull(parallelMapper)) {
            List<List<? extends T>> chunks = getChunk(countThread, list);
            return map(currentList -> currentList.stream().min(comparator).get(), chunks)
                    .stream()
                    .min(comparator)
                    .get();
        } else {
            List<Callable<T>> tasks = getTasks(countThread, list, currentList -> currentList.stream().min(comparator).get());
            runAll(tasks.stream().map(Thread::new).collect(Collectors.toList()));
            return tasks.stream().map(Callable::getResponse).min(comparator).get();
        }
    }

    @Override
    public <T> boolean all(int countThread, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        if (Objects.nonNull(parallelMapper)) {
            List<List<? extends T>> chunks = getChunk(countThread, list);
            return map(currentList -> currentList.stream().allMatch(predicate), chunks)
                    .stream()
                    .allMatch(value -> value);
        } else {
            List<Callable<Boolean>> tasks = getTasks(countThread, list, currentList -> currentList.stream().allMatch(predicate));
            runAll(tasks.stream().map(Thread::new).collect(Collectors.toList()));
            return tasks.stream().allMatch(Callable::getResponse);
        }
    }

    @Override
    public <T> boolean any(int countThread, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        if (Objects.nonNull(parallelMapper)) {
            List<List<? extends T>> chunks = getChunk(countThread, list);
            return map(currentList -> currentList.stream().anyMatch(predicate), chunks)
                    .stream()
                    .anyMatch(value -> value);
        } else {
            List<Callable<Boolean>> tasks = getTasks(countThread, list, currentList -> currentList.stream().anyMatch(predicate));
            runAll(tasks.stream().map(Thread::new).collect(Collectors.toList()));
            return tasks.stream().anyMatch(Callable::getResponse);
        }
    }

    private <T> List<List<? extends T>> getChunk(int countThread, List<? extends T> list) {
        int countElement = countThread > list.size() ? 1 : list.size() / countThread;
        int countActiveThread = Math.min(countThread, list.size());
        return IntStream.range(0, countActiveThread).boxed().map(currentThread -> {
                    int to = currentThread * countElement;
                    int from = (currentThread == countActiveThread - 1) ? list.size() : to + countElement;
                    return list.subList(to, from);
                }
        ).collect(Collectors.toList());
    }

    private <T, R> List<R> map(Function<? super T, ? extends R> function, List<? extends T> list) {
        try {
            return parallelMapper.map(function, list);
        } catch (InterruptedException e) {
            return Collections.emptyList();
        }
    }

    private <R, T> List<Callable<R>> getTasks(int countThread, List<T> list, Function<List<T>, R> function) {
        int countElement = countThread > list.size() ? 1 : list.size() / countThread;
        int countActiveThread = Math.min(countThread, list.size());
        return IntStream.range(0, countActiveThread).boxed().map(currentThread -> {
                    int to = currentThread * countElement;
                    int from = (currentThread == countActiveThread - 1) ? list.size() : to + countElement;
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

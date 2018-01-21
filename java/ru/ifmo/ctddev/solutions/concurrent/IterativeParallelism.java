package ru.ifmo.ctddev.solutions.concurrent;


import info.kgeorgiy.java.advanced.concurrent.ListIP;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IterativeParallelism implements ListIP {

    @Override
    public String join(int countThread, List<?> list) throws InterruptedException {
        List<Callable<String>> tasks = getTasks(countThread, list, currentList -> currentList.stream().map(Object::toString).collect(Collectors.joining()));
        runAll(tasks.stream().map(Thread::new).collect(Collectors.toList()));
        return tasks.stream().map(Callable::getResponse).collect(Collectors.joining());
    }

    @Override
    public <T> List<T> filter(int countThread, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        List<Callable<List<? extends T>>> tasks = getTasks(countThread, list, currentList -> currentList.stream().filter(predicate).collect(Collectors.toList()));
        runAll(tasks.stream().map(Thread::new).collect(Collectors.toList()));
        return tasks.stream().map(Callable::getResponse).collect(ArrayList::new, List::addAll, List::addAll);
    }

    @Override
    public <T, U> List<U> map(int countThread, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        List<Callable<List<? extends U>>> tasks = getTasks(countThread, list, currentList -> currentList.stream().map(function).collect(Collectors.toList()));
        runAll(tasks.stream().map(Thread::new).collect(Collectors.toList()));
        return tasks.stream().map(Callable::getResponse).collect(ArrayList::new, List::addAll, List::addAll);
    }

    @Override
    public <T> T maximum(int countThread, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        List<Callable<T>> tasks = getTasks(countThread, list, currentList -> currentList.stream().max(comparator).get());
        runAll(tasks.stream().map(Thread::new).collect(Collectors.toList()));
        return tasks.stream().map(Callable::getResponse).max(comparator).get();
    }

    @Override
    public <T> T minimum(int countThread, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        List<Callable<T>> tasks = getTasks(countThread, list, currentList -> currentList.stream().min(comparator).get());
        runAll(tasks.stream().map(Thread::new).collect(Collectors.toList()));
        return tasks.stream().map(Callable::getResponse).min(comparator).get();
    }

    @Override
    public <T> boolean all(int countThread, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        List<Callable<Boolean>> tasks = getTasks(countThread, list, currentList -> currentList.stream().allMatch(predicate));
        runAll(tasks.stream().map(Thread::new).collect(Collectors.toList()));
        return tasks.stream().allMatch(Callable::getResponse);
    }

    @Override
    public <T> boolean any(int countThread, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        List<Callable<Boolean>> tasks = getTasks(countThread, list, currentList -> currentList.stream().anyMatch(predicate));
        runAll(tasks.stream().map(Thread::new).collect(Collectors.toList()));
        return tasks.stream().anyMatch(Callable::getResponse);
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
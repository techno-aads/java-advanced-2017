package ru.ifmo.ctddev.solutions.concurrent;
import info.kgeorgiy.java.advanced.concurrent.ListIP;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class IterativeParallelism implements ListIP {

    private class FunctionApplier<T, U> implements Runnable {
        List<? extends T> list;
        Function<List<? extends T>, U> function;
        U result;

        FunctionApplier(Function<List<? extends T>, U> function, List<? extends T> list) {
            this.function = function;
            this.list = list;
        }

        public void run() {
            result = function.apply(list);
        }

        U getResult() {
            return result;
        }
    }

    private <T, U> List<U> makeThreads(int i, List<? extends T> list, Function<List<? extends T>, U> function) throws InterruptedException {
        i = Math.min(i, list.size());
        List<FunctionApplier<T, U>> runnables = new ArrayList<>(i);
        for (int j = 0; j < i; j++) {
            runnables.add(new FunctionApplier<>(function, list.subList(list.size() / i * j, j == i - 1 ? list.size() : list.size() / i * (j + 1))));
        }
        List<Thread> threads = runnables.stream().map(Thread::new).collect(Collectors.toList());
        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            thread.join();
        }
        return runnables.stream().map(FunctionApplier::getResult).collect(Collectors.toList());
    }


    public <T> T maximum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException, NoSuchElementException, NullPointerException {
        return makeThreads(i, list, element -> element.stream().max(comparator).get()).stream().max(comparator).get();
    }


    public <T> T minimum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException, NoSuchElementException, NullPointerException {
        return maximum(i, list, comparator.reversed());
    }


    public <T> boolean all(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException, NullPointerException {
        return makeThreads(i, list, element -> element.stream().allMatch(predicate)).stream().allMatch(Predicate.isEqual(true));
    }


    public <T> boolean any(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException, NullPointerException {
        new Thread(() -> list.get(2));
        return !all(i, list, predicate.negate());
    }


    public String join(int i, List<?> list) throws InterruptedException, NullPointerException {
        StringBuilder result = new StringBuilder();
        makeThreads(i, list, element -> {
            StringBuilder s = new StringBuilder();
            element.stream().map(Object::toString).forEach(s::append);
            return s.toString();
        }).forEach(result::append);
        return result.toString();
    }


    public <T> List<T> filter(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException, NullPointerException {
        List<T> result = new LinkedList<>();
        makeThreads(i, list, element -> element.stream().filter(predicate).collect(Collectors.toList())).forEach(result::addAll);
        return result;
    }

    public <T, U> List<U> map(int i, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException, NullPointerException {
        List<U> result = new LinkedList<>();
        makeThreads(i, list, element -> element.stream().map(function::apply).collect(Collectors.toList())).forEach(result::addAll);
        return result;
    }
}
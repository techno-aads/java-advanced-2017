package ru.ifmo.ctddev.solutions.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class IterativeParallelism implements ListIP {

    public <T> T maximum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        int taskNum = getTaskNumber(i, list);
        List<T> threadResults = new ArrayList<>(Collections.nCopies(taskNum, null));

        Function<List<? extends T>, T> f = (l -> Collections.max(l, comparator));

        List<Runnable> tasks = splitAndPrepareTasks(list, taskNum, threadResults, f);
        runAllTask(tasks);

        return Collections.max(threadResults, comparator);
    }

    public <T> T minimum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        int taskNum = getTaskNumber(i, list);
        List<T> threadResults = new ArrayList<>(Collections.nCopies(taskNum, null));

        Function<List<? extends T>, T> f = l -> Collections.min(l, comparator);

        List<Runnable> tasks = splitAndPrepareTasks(list, taskNum, threadResults, f);
        runAllTask(tasks);

        return Collections.min(threadResults, comparator);
    }

    public <T> boolean all(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        int taskNum = getTaskNumber(i, list);
        List<Boolean> threadResults = new ArrayList<>(Collections.nCopies(taskNum, null));

        Function<List<? extends T>, Boolean> f = l -> l.stream().allMatch(predicate);

        List<Runnable> tasks = splitAndPrepareTasks(list, taskNum, threadResults, f);
        runAllTask(tasks);

        return threadResults.stream().allMatch(value -> value);

    }

    public <T> boolean any(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        int taskNum = getTaskNumber(i, list);
        List<Boolean> threadResults = new ArrayList<>(Collections.nCopies(taskNum, null));

        Function<List<? extends T>, Boolean> f = l -> l.stream().anyMatch(predicate);

        List<Runnable> tasks = splitAndPrepareTasks(list, taskNum, threadResults, f);
        runAllTask(tasks);

        return threadResults.stream().anyMatch(value -> value);
    }

    @Override
    public String join(int i, List<?> list) throws InterruptedException {
        int taskNum = getTaskNumber(i, list);
        List<String> threadResults = new ArrayList<>(Collections.nCopies(taskNum, null));

        Function<List<?>, String> f = l -> l.stream().map(Object::toString).collect(Collectors.joining());

        List<Runnable> tasks = splitAndPrepareTasks(list, taskNum, threadResults, f);
        runAllTask(tasks);

        return threadResults.stream().collect(Collectors.joining());
    }

    @Override
    public <T> List<T> filter(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        int taskNum = getTaskNumber(i, list);
        List<List<T>> threadResults = new ArrayList<>(Collections.nCopies(taskNum, null));

        Function<List<? extends T>, List<T>> f = l -> l.stream().filter(predicate).collect(Collectors.toList());

        List<Runnable> tasks = splitAndPrepareTasks(list, taskNum, threadResults, f);
        runAllTask(tasks);

        return threadResults.stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public <T, U> List<U> map(int i, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        int taskNum = getTaskNumber(i, list);
        List<List<U>> threadResults = new ArrayList<>(Collections.nCopies(taskNum, null));

        Function<List<? extends T>, List<U>> f = l -> l.stream().map(function).collect(Collectors.toList());

        List<Runnable> tasks = splitAndPrepareTasks(list, taskNum, threadResults, f);
        runAllTask(tasks);

        return threadResults.stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    private <T> int getTaskNumber(int i, List<? extends T> list) {
        return list.size() < i ? list.size() : i;
    }

    private <T, R> List<Runnable> splitAndPrepareTasks(List<? extends T> elements, int taskNum,
                                                       List<R> threadResults, Function<List<? extends T>, R> function) {
        List<Runnable> tasks = new ArrayList<>();

        int subListLength = elements.size() / taskNum;
        for (int i = 0; i < taskNum; i++) {
            int task = i;
            tasks.add(() -> {
                int fromIndex = subListLength * task;
                int toIndex = task == taskNum - 1 ? elements.size() : fromIndex + subListLength;
                threadResults.set(task, function.apply(elements.subList(fromIndex, toIndex)));
            });

        }

        return tasks;
    }

    private void runAllTask(List<Runnable> tasks) throws InterruptedException {
        Thread[] threads = new Thread[tasks.size()];

        for (int i = 0; i < tasks.size(); i++) {
            Thread thread = new Thread(tasks.get(i));
            thread.start();
            threads[i] = thread;
        }

        for (Thread thread : threads) {
            thread.join();
        }
    }
}
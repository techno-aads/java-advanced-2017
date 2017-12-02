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

        List<Runnable> tasks = new ArrayList<>();
        int subListLength = list.size() / taskNum;
        for (i = 0; i < taskNum; i++) {
            int task = i;
            tasks.add(() -> {
                int fromIndex = subListLength * task;
                int toIndex = task == taskNum - 1 ? list.size() : fromIndex + subListLength;
                threadResults.set(task, Collections.max(list.subList(fromIndex, toIndex), comparator));
            });
        }

        runAllTask(tasks);
        return Collections.max(threadResults, comparator);
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


    private <T> int getTaskNumber(int i, List<? extends T> list) {
        return list.size() < i ? list.size() : i;
    }

    public <T> T minimum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        int taskNum = getTaskNumber(i, list);
        List<T> threadResults = new ArrayList<>(Collections.nCopies(taskNum, null));


        List<Runnable> tasks = new ArrayList<>();
        int subListLength = list.size() / taskNum;
        for (i = 0; i < taskNum; i++) {
            int task = i;
            tasks.add(() -> {
                int fromIndex = subListLength * task;
                int toIndex = task == taskNum - 1 ? list.size() : fromIndex + subListLength;
                T min = list.get(fromIndex);
                for (int j = fromIndex + 1; j < toIndex; ++j) {
                    if (comparator.compare(min, list.get(j)) > 0) {
                        min = list.get(j);
                    }
                }
                threadResults.set(task, min);
            });
        }

        runAllTask(tasks);
        return Collections.min(threadResults, comparator);
    }

    public <T> boolean all(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        int taskNum = getTaskNumber(i, list);
        List<Boolean> threadResults = new ArrayList<>(Collections.nCopies(taskNum, null));

        List<Runnable> tasks = new ArrayList<>();
        int subListLength = list.size() / taskNum;
        for (i = 0; i < taskNum; i++) {
            int task = i;
            tasks.add(() -> {
                int fromIndex = subListLength * task;
                int toIndex = task == taskNum - 1 ? list.size() : fromIndex + subListLength;
                threadResults.set(task, list.subList(fromIndex, toIndex).stream().allMatch(predicate));
            });
        }

        runAllTask(tasks);
        return threadResults.stream().allMatch(value -> value);

    }

    public <T> boolean any(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        int taskNum = getTaskNumber(i, list);
        List<Boolean> threadResults = new ArrayList<>(Collections.nCopies(taskNum, null));

        List<Runnable> tasks = new ArrayList<>();
        int subListLength = list.size() / taskNum;
        for (i = 0; i < taskNum; i++) {
            int task = i;
            tasks.add(() -> {
                int fromIndex = subListLength * task;
                int toIndex = task == taskNum - 1 ? list.size() : fromIndex + subListLength;
                threadResults.set(task, list.subList(fromIndex, toIndex).stream().anyMatch(predicate));
            });

        }

        runAllTask(tasks);
        return threadResults.stream().anyMatch(value -> value);
    }

    @Override
    public String join(int i, List<?> list) throws InterruptedException {
        int taskNum = getTaskNumber(i, list);
        List<String> threadResults = new ArrayList<>(Collections.nCopies(taskNum, null));

        List<Runnable> tasks = new ArrayList<>();
        int subListLength = list.size() / taskNum;
        for (i = 0; i < taskNum; i++) {
            int task = i;
            tasks.add(() -> {
                int fromIndex = subListLength * task;
                int toIndex = task == taskNum - 1 ? list.size() : fromIndex + subListLength;
                StringBuilder result = new StringBuilder();
                list.subList(fromIndex, toIndex).forEach(result::append);
                threadResults.set(task, result.toString());
            });

        }

        runAllTask(tasks);
        return threadResults.stream().collect(Collectors.joining());
    }

    @Override
    public <T> List<T> filter(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        int taskNum = getTaskNumber(i, list);
        List<List<T>> threadResults = new ArrayList<>(Collections.nCopies(taskNum, null));

        List<Runnable> tasks = new ArrayList<>();
        int subListLength = list.size() / taskNum;
        for (i = 0; i < taskNum; i++) {
            int task = i;
            tasks.add(() -> {
                int fromIndex = subListLength * task;
                int toIndex = task == taskNum - 1 ? list.size() : fromIndex + subListLength;
                List<T> result = list.subList(fromIndex, toIndex).stream()
                        .filter(predicate).collect(Collectors.toList());
                threadResults.set(task, result);
            });

        }

        runAllTask(tasks);
        return threadResults.stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public <T, U> List<U> map(int i, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        int taskNum = getTaskNumber(i, list);
        List<List<U>> threadResults = new ArrayList<>(Collections.nCopies(taskNum, null));

        List<Runnable> tasks = new ArrayList<>();
        int subListLength = list.size() / taskNum;
        for (i = 0; i < taskNum; i++) {
            int task = i;
            tasks.add(() -> {
                int fromIndex = subListLength * task;
                int toIndex = task == taskNum - 1 ? list.size() : fromIndex + subListLength;
                List<U> result = list.subList(fromIndex, toIndex).stream()
                        .map(function::apply).collect(Collectors.toList());
                threadResults.set(task, result);
            });

        }

        runAllTask(tasks);
        return threadResults.stream().flatMap(Collection::stream).collect(Collectors.toList());
    }
}
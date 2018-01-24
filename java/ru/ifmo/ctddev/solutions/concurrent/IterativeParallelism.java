package ru.ifmo.ctddev.solutions.concurrent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

public class IterativeParallelism implements ListIP {

    abstract class Job<T> implements Runnable {
        private T result = null;

        T getResult() {
            return result;
        }

        void setResult(T response) {
            this.result = response;
        }
    }

    private <R, T> List<Job<R>> splitByJobs(int countThread, List<T> list, Function<List<T>, R> function) {
        int elementsInBatch = countThread > list.size() ? 1 : list.size() / countThread;
        int countActiveThread = Math.min(countThread, list.size());
        return IntStream.range(0, countActiveThread).boxed().map(currentThread -> {
                    int to = currentThread * elementsInBatch;
                    int from = (currentThread == countActiveThread - 1) ? list.size() : to + elementsInBatch;
                    return defineJob(list, function, to, from);
                }
        ).collect(Collectors.toList());
    }

    private <R, T> IterativeParallelism.Job<R> defineJob(List<T> list, Function<List<T>, R> function, int to, int from) {
        return new Job<R>() {
            @Override
            public void run() {
                List<T> batch = list.subList(to, from);
                setResult(function.apply(batch));
            }
        };
    }

    private void startAndWaitThreads(List<Thread> threads) throws InterruptedException {
        threads.forEach(Thread::start);
        for (Thread t : threads) {
            t.join();
        }
    }

    @Override
    public <T> T maximum(int countThread, List<? extends T> list, Comparator<? super T> comparator)
            throws InterruptedException {
        List<Job<T>> jobs = splitByJobs(countThread, list, currentList -> currentList.stream().max(comparator).get());
        startAndWaitThreads(jobs.stream().map(Thread::new).collect(Collectors.toList()));
        return jobs.stream().map(Job::getResult).max(comparator).get();
    }

    @Override
    public <T> T minimum(int countThread, List<? extends T> list, Comparator<? super T> comparator)
            throws InterruptedException {
        List<Job<T>> jobs = splitByJobs(countThread, list, currentList -> currentList.stream().min(comparator).get());
        startAndWaitThreads(jobs.stream().map(Thread::new).collect(Collectors.toList()));
        return jobs.stream().map(Job::getResult).min(comparator).get();
    }

    @Override
    public <T> boolean all(int countThread, List<? extends T> list, Predicate<? super T> predicate)
            throws InterruptedException {
        List<Job<Boolean>> jobs = splitByJobs(countThread, list,
                currentList -> currentList.stream().allMatch(predicate));
        startAndWaitThreads(jobs.stream().map(Thread::new).collect(Collectors.toList()));
        return jobs.stream().allMatch(Job::getResult);
    }

    @Override
    public <T> boolean any(int countThread, List<? extends T> list, Predicate<? super T> predicate)
            throws InterruptedException {
        List<Job<Boolean>> jobs = splitByJobs(countThread, list,
                currentList -> currentList.stream().anyMatch(predicate));
        startAndWaitThreads(jobs.stream().map(Thread::new).collect(Collectors.toList()));
        return jobs.stream().anyMatch(Job::getResult);
    }

    @Override
    public String join(int countThread, List<?> list) throws InterruptedException {
        List<Job<String>> jobs = splitByJobs(countThread, list,
                currentList -> currentList.stream().map(Object::toString).collect(Collectors.joining()));
        startAndWaitThreads(jobs.stream().map(Thread::new).collect(Collectors.toList()));
        return jobs.stream().map(Job::getResult).collect(Collectors.joining());
    }

    @Override
    public <T> List<T> filter(int countThread, List<? extends T> list, Predicate<? super T> predicate)
            throws InterruptedException {
        List<Job<List<? extends T>>> jobs = splitByJobs(countThread, list,
                currentList -> currentList.stream().filter(predicate).collect(Collectors.toList()));
        startAndWaitThreads(jobs.stream().map(Thread::new).collect(Collectors.toList()));
        return jobs.stream().map(Job::getResult).collect(ArrayList::new, List::addAll, List::addAll);
    }

    @Override
    public <T, U> List<U> map(int countThread, List<? extends T> list, Function<? super T, ? extends U> function)
            throws InterruptedException {
        List<Job<List<? extends U>>> jobs = splitByJobs(countThread, list,
                currentList -> currentList.stream().map(function).collect(Collectors.toList()));
        startAndWaitThreads(jobs.stream().map(Thread::new).collect(Collectors.toList()));
        return jobs.stream().map(Job::getResult).collect(ArrayList::new, List::addAll, List::addAll);
    }
}
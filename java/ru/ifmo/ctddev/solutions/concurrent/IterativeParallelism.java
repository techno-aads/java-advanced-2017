package ru.ifmo.ctddev.solutions.concurrent;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Predicate;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;

public class IterativeParallelism implements ScalarIP {

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        ArrayBlockingQueue<T> resFromWorkers = new ArrayBlockingQueue<>(threads);
        List<Runnable> workers = new ArrayList<>();
        List<List<? extends T>> splited = split(threads, values);
        for(int i = 0; i < threads; ++i) {
            workers.add(new WorkerWithComparator<T>(splited.get(i), comparator
                    , WorkerWithComparator.ComparatorType.MAX, resFromWorkers));
        }
        parallelWork(workers);

        return Collections.max(resFromWorkers, comparator);
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        ArrayBlockingQueue<T> resFromWorkers = new ArrayBlockingQueue<>(threads);
        List<Runnable> workers = new ArrayList<>();
        List<List<? extends T>> splited = split(threads, values);
        for(int i = 0; i < threads; ++i) {
            workers.add(new WorkerWithComparator<T>(splited.get(i), comparator
                    , WorkerWithComparator.ComparatorType.MIN, resFromWorkers));
        }
        parallelWork(workers);

        return Collections.min(resFromWorkers, comparator);
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        ArrayBlockingQueue<Boolean> resFromWorkers = new ArrayBlockingQueue<>(threads);
        List<Runnable> workers = new ArrayList<>();
        List<List<? extends T>> splited = split(threads, values);
        for(int i = 0; i < threads; ++i) {
            workers.add(new WorkerWithPredicate<T>(splited.get(i), predicate
                    , WorkerWithPredicate.PredicateType.ALL, resFromWorkers));
        }
        parallelWork(workers);

        for(boolean val : resFromWorkers) {
            if(!val)
                return false;
        }
        return true;
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        ArrayBlockingQueue<Boolean> resFromWorkers = new ArrayBlockingQueue<>(threads);
        List<Runnable> workers = new ArrayList<>();
        List<List<? extends T>> splited = split(threads, values);
        for(int i = 0; i < threads; ++i) {
            workers.add(new WorkerWithPredicate<T>(splited.get(i), predicate
                    , WorkerWithPredicate.PredicateType.ANY, resFromWorkers));
        }
        parallelWork(workers);

        for(boolean val : resFromWorkers) {
            if(val)
                return true;
        }
        return false;
    }

    private <T> List<List<? extends T>> split(int count, List<? extends T> list) {
        List<List<? extends T>> res = new ArrayList<>();
        int partSize = list.size() / count;
        for(int i = 0; i < count - 1; ++i) {
            res.add(list.subList(partSize * i, partSize * i + partSize));
        }
        res.add(list.subList(partSize * (count - 1), list.size()));
        return res;
    }

    private void parallelWork(List<Runnable> workers) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        for(Runnable worker : workers) {
            Thread t = new Thread(worker);
            threads.add(t);
            t.start();
        }
        for(Thread t : threads) {
            t.join();
        }
    }

}

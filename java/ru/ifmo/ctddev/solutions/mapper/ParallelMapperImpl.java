package ru.ifmo.ctddev.solutions.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ParallelMapperImpl implements ParallelMapper {
    private ExecutorService executor;
    private int threads;
    private Lock locker = new ReentrantLock(true);

    public ParallelMapperImpl(int threads) {
        int maxThread = Runtime.getRuntime().availableProcessors();
        this.threads = Math.min(maxThread, threads);

        executor = Executors.newWorkStealingPool(this.threads);
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        //todo
        //
        ArrayList<Future<List<R>>> futures = new ArrayList<>();
        locker.lock();
        try {
            int sizeSubList = args.size() / threads;
            for (int i = 0; i < threads; i++) {
                int localI = i;
                Future<List<R>> future = executor.submit(() -> {
                    int startIndex = sizeSubList * localI;
                    int endIndex = (localI == threads - 1) ? args.size() : startIndex + sizeSubList;
                    return args.subList(startIndex, endIndex).stream().map(f).collect(Collectors.toList());
                });
                futures.add(future);
            }
        } finally {
            locker.unlock();
        }

        ArrayList<R> results = new ArrayList<>();
        for (Future<List<R>> future : futures) {
            try {
                results.addAll(future.get());
            } catch (ExecutionException ex) {
                throw new InterruptedException(ex.getMessage());
            }
        }

        return results;
    }

    @Override
    public void close() throws InterruptedException {
        executor.shutdownNow();
    }
}

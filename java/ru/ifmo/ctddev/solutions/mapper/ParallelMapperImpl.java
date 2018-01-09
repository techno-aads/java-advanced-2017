package ru.ifmo.ctddev.solutions.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

public class ParallelMapperImpl implements ParallelMapper {
    private ExecutorService executor;
    private int threads;
    private Lock locker = new ReentrantLock(true);

    public ParallelMapperImpl (int threads){
        int maxThread =  Runtime.getRuntime().availableProcessors();
        this.threads = Math.min(maxThread, threads);

        executor = Executors.newWorkStealingPool(this.threads);
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        //todo
        locker.lock();
        try{
            ArrayList<Future<List<R>>> futures = new ArrayList<>();
            int sizeSubList = args.size() / threads;
            for (int i =0; i < threads; i++) {
                int localI = i;
                Future<List<R>> future = executor.submit( ()->{
                    int startIndex = sizeSubList * localI;
                    int endIndex = (localI == threads - 1) ? args.size() : startIndex + sizeSubList;
                    return args.subList(startIndex,endIndex).stream().map(f).collect(Collectors.toList());
                });
                futures.add(future);
            }

            ArrayList<R> results = new ArrayList<>();
            for (Future<List<R>> future : futures) {
                try {
                    results.addAll(future.get());
                }
                catch (ExecutionException ex){
                    throw new InterruptedException(ex.getMessage());
                }
            }

            return results;
        }
        finally {
            locker.unlock();
        }
    }

    @Override
    public void close() throws InterruptedException {
        executor.shutdownNow();
    }
}

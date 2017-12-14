package ru.ifmo.ctddev.solutions.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class ParallelMapperImpl implements ParallelMapper {

    private final Lock lock = new ReentrantLock(true);
    private ExecutorService executorService;

    public ParallelMapperImpl(int threadCount) {
        executorService =  Executors.newFixedThreadPool(threadCount > 1 ? threadCount : 1);
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> function, List<? extends T> list) throws InterruptedException {
        lock.lock();
        try{
            List<Future<R>> futureList = new ArrayList<>();

            for (int i = 0; i < list.size(); i++) {
                int index = i;

                futureList.add(executorService.submit(new Callable<R>() {
                    @Override
                    public R call() throws Exception {
                        return function.apply(list.get(index));
                    }
                }));
            }
            List<R> result = new ArrayList<>();

            for (Future<R> future : futureList) {
                result.add(future.get());
            }
            return result;

        } catch (ExecutionException e) {
            throw new IllegalArgumentException(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() throws InterruptedException {
        executorService.shutdownNow();
    }
}


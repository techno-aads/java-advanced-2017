package ru.ifmo.ctddev.solutions.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;

public class ParallelMapperImpl implements ParallelMapper {
    private final ExecutorService executorService;

    public ParallelMapperImpl(int threads) {
        executorService = Executors.newFixedThreadPool(threads);
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> function, List<? extends T> list) throws InterruptedException {
        List<Future<R>> tasks = new ArrayList<>(list.size());

        synchronized (executorService) {
            for (T value: list) tasks.add(executorService.submit(() -> function.apply(value)));
        }

        List<R> results = new ArrayList<>(list.size());
        try {
            for (Future<R> f: tasks) {
                try { results.add(f.get()); }
                catch (CancellationException | ExecutionException e) { results.add(null); }
            }

            return results;
        }
        finally {
            if (results.size() == list.size()) for (Future<R> f : tasks) f.cancel(true);
        }
    }

    @Override
    public void close() { executorService.shutdownNow(); }
}

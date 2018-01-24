package ru.ifmo.ctddev.solutions.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Nikita Sokeran
 */
public class ParallelMapperImpl implements ParallelMapper {
    private final ExecutorService executor;

    public ParallelMapperImpl(final int threads) {
        this.executor = Executors.newFixedThreadPool(threads);
    }

    @Override
    public <T, R> List<R> map(final Function<? super T, ? extends R> mapper,
                              final List<? extends T> args) throws InterruptedException {
        final List<Callable<R>> tasks = args.stream()
                .map(arg -> (Callable<R>) () -> mapper.apply(arg))
                .collect(Collectors.toList());

        final List<Future<R>> futures = executor.invokeAll(tasks);

        final List<R> results = futures.stream()
            .map(future -> {
                try {
                    return future.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new IllegalStateException("Couldn't execute map task.", e);
                }
            })
            .collect(Collectors.toList());

        return results;
    }

    @Override
    public void close() throws InterruptedException {
        executor.shutdown();
        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }
    }
}
package ru.ifmo.ctddev.solutions.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ParallelMapperImpl implements ParallelMapper {

    private ExecutorService executorService;

    public ParallelMapperImpl() {
        executorService = Executors.newSingleThreadExecutor();
    }

    public ParallelMapperImpl(int thread) {
        executorService = Executors.newFixedThreadPool(thread);
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> function, List<? extends T> list) throws InterruptedException {
        List<CompletableFuture<? extends R>> completableFutureList = list.stream()
                .map(item -> CompletableFuture.supplyAsync(() -> function.apply(item), executorService))
                .collect(Collectors.<CompletableFuture<? extends R>>toList());
        CompletableFuture.allOf(completableFutureList.toArray(new CompletableFuture[completableFutureList.size()])).join();
        return completableFutureList.stream().map(CompletableFuture::join).collect(Collectors.toList());
    }

    @Override
    public void close() throws InterruptedException {
        executorService.shutdownNow();
    }
}

package ru.ifmo.ctddev.solutions.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ParallelMapperImpl implements ParallelMapper {
    private ExecutorService m_Executor;
    private int m_Threads;

    public ParallelMapperImpl(int threads) {
        int maxAvailableThread = Runtime.getRuntime().availableProcessors();
        m_Threads = Math.min(maxAvailableThread, threads);

        m_Executor = Executors.newWorkStealingPool(m_Threads);
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {

        ArrayList<Future<List<R>>> futures = new ArrayList<>();

        int argumnetSize = args.size();
        int sizeSubList = argumnetSize / m_Threads;
        for (int i = 0; i < m_Threads; i++) {
            int localI = i;
            Future<List<R>> future = m_Executor.submit(() -> {
                int startIndex = sizeSubList * localI;
                int endIndex = (localI == m_Threads - 1) ? args.size() : startIndex + sizeSubList;
                return args.subList(startIndex, endIndex).stream().map(f).collect(Collectors.toList());
            });
            futures.add(future);
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
        m_Executor.shutdownNow();
    }
}
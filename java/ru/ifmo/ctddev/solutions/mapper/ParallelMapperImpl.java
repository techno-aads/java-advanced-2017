package ru.ifmo.ctddev.solutions.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ParallelMapperImpl implements ParallelMapper {
    /**
     * Maximum number of threads that allowed to create
     */
    private final int workersNum;

    /**
     * Each thread who wants to invoke mapping task should acquire this lock and holds It until the task in complete.
     * It allows to build the fair queue of tasks that desires to to mapping.
     *
     * Actually I'm not sure It is required. Because without all works in same way
     */
    private final Lock lock = new ReentrantLock(true);

    /**
     * Using for parallel invocation of mapping subtasks
     */
    private final ExecutorService subtaskExecutor;

    public ParallelMapperImpl(int workersNum) {
        this.workersNum = workersNum;
        subtaskExecutor = Executors.newWorkStealingPool(workersNum);
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> function, List<? extends T> list)
            throws InterruptedException {
        Objects.requireNonNull(function);
        Objects.requireNonNull(list);

        lock.lock();
        try {
            ArrayList<Future<List<R>>> futures = new ArrayList<>();

            int subListLength = list.size() / workersNum;
            for (int i = 0; i < workersNum; i++) {
                int task = i;
                Future<List<R>> future = subtaskExecutor.submit(() -> {
                    int fromIndex = subListLength * task;
                    int toIndex = task == workersNum - 1 ? list.size() : fromIndex + subListLength;
                    return list.subList(fromIndex, toIndex).stream()
                            .map(function::apply).collect(Collectors.toList());
                });
                futures.add(future);
            }

            ArrayList<R> result = new ArrayList<>();
            for (Future<List<R>> future : futures) {
                try {
                    result.addAll(future.get());
                } catch (ExecutionException e) {
                    throw new IllegalStateException("Something went wrong : ", e);
                }
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() throws InterruptedException {
        subtaskExecutor.shutdownNow();
    }
}
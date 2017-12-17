package ru.ifmo.ctddev.solutions.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author zakhar.razzhivin
 */
public class ParallelMapperImpl implements ParallelMapper {

    private List<Thread> threads = new ArrayList<>();
    private Queue queue = new Queue();

    public ParallelMapperImpl(int threadsNum) {
        for (int i = 0; i < threadsNum; i++) {
            Thread thread = new Thread(() -> {
                try {
                    while (!Thread.interrupted()) {
                        queue.getNextTask().process();
                    }
                } catch (InterruptedException exp) {
                }
            });

            threads.add(thread);
            thread.start();
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> function, List<? extends T> list) throws InterruptedException {
        List<Queue.Task<? super T, ? extends R>> tasks = new ArrayList<>();
        for (T t : list) {
            Queue.Task<? super T, ? extends R> task = new Queue.Task<>(t, function);
            queue.addTask(task);
            tasks.add(task);
        }

        List<R> results = new ArrayList<>();
        for (Queue.Task<? super T, ? extends R> task : tasks) {
            results.add(task.getResult());
        }
        return results;
    }

    @Override
    public void close() throws InterruptedException {
        for (Thread thread : threads) {
            thread.interrupt();
        }
    }
}

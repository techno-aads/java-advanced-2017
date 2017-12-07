package ru.ifmo.ctddev.solutions.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class ParallelMapperImpl implements ParallelMapper {

    private List<Worker> workers = new ArrayList<>();
    private SafeQueue queue = new SafeQueue();

    public ParallelMapperImpl(int threads) {
        for (int i = 0; i < threads; i++) {
            Worker worker = new Worker(queue);
            workers.add(worker);
            worker.start();
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> function, List<? extends T> list) throws InterruptedException {
        List<Task<? super T, ? extends R>> tasks = new ArrayList<>();
        for (T arg : list) {
            Task<? super T, ? extends R> task = new Task<>(arg, function);
            queue.add(task);
            tasks.add(task);
        }
        List<R> totalResult = new ArrayList<>();
        for (Task<? super T, ? extends R> task : tasks) {
            totalResult.add(task.getResult());
        }
        return totalResult;
    }

    @Override
    public void close() {
        for (Worker worker : workers) {
            worker.interrupt();
        }
    }

    public static class SafeQueue {
        final Queue<Task> queue = new LinkedList<>();

        private synchronized boolean add(Task task) {
            boolean result = queue.add(task);
            notifyAll();
            return result;
        }

        synchronized private Task poll() throws InterruptedException {
            while (queue.isEmpty()) {
                wait();
            }
            return queue.poll();
        }
    }

    private static class Task<T, R> {

        private T argument;
        private Function<? super T, ? extends R> function;
        private R result;

        Task(T argument, Function<? super T, ? extends R> function) {
            this.argument = argument;
            this.function = function;
        }

        public synchronized void execute() {
            result = function.apply(argument);
            notifyAll();
        }

        public synchronized R getResult() throws InterruptedException {
            while (result == null) {
                wait();
            }
            return result;
        }

    }

    private static class Worker extends Thread {

        private final SafeQueue queue;

        private Worker(SafeQueue queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    if (Thread.interrupted()) {
                        break;
                    }
                    queue.poll().execute();
                }
            } catch (InterruptedException ignored) {
            }
        }
    }
}
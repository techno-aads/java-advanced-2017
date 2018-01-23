package ru.ifmo.ctddev.solutions.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;


public class ParallelMapperImpl implements ParallelMapper {

    private volatile boolean isTerminated = false;
    private final ArrayDeque<Consumer<Void>> queue;
    private final Thread[] threads;


    public ParallelMapperImpl(int threadCount) {

        queue = new ArrayDeque<>();
        threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            this.threads[i] = new Thread(() -> {

                while (!isTerminated) {

                    Consumer<Void> data = null;
                    synchronized (queue) {
                        if (!queue.isEmpty()) {
                            data = queue.pop();
                        }
                    }

                    if (data != null) {

                        data.accept(null);
                        synchronized (queue) {
                            queue.notifyAll();
                        }

                    }else{

                        try {
                            synchronized (queue) {
                                queue.wait();
                            }
                        } catch (InterruptedException ignored) {
                            return;
                        }

                    }

                }

            });
            this.threads[i].start();
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {

        if (isTerminated) throw new IllegalStateException("ParallelMapperImpl was already closed");
        final int argsize = args.size();
        AtomicInteger counter = new AtomicInteger(0);
        ArrayList<R> resultList = new ArrayList<>(args.size());
        for (int i = 0; i < argsize; i++) resultList.add(null);

        for (int i = 0; i < argsize; i++) {
            final int ind = i;
            synchronized (queue) {
                queue.push((whatever) -> {
                    T elem;
                    elem = args.get(ind);
                    R res = f.apply(elem);
                    synchronized (resultList) {
                        resultList.set(ind, res);
                    }
                    counter.incrementAndGet();
                    synchronized (queue) {
                        queue.notifyAll();
                    }
                });
            }
        }
        synchronized (queue) {
            queue.notifyAll();
            while (counter.get() < argsize) {
                queue.wait();
            }
        }
        return resultList;
    }


    @Override
    public void close() throws InterruptedException {
        isTerminated = true;
        for (Thread i : threads) {
            i.interrupt();
            i.join();
        }
    }
}
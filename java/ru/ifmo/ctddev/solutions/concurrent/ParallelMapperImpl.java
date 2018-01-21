package ru.ifmo.ctddev.chernyatskiy.IterativeParallelism;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;


public class ParallelMapperImpl implements ParallelMapper {

    private final ArrayDeque<Consumer<Void>> Q;
    private final Thread[] Thrds;
    private volatile boolean TermFlag = false;


    public ParallelMapperImpl(int ThrCount) {

        Q = new ArrayDeque<>();
        Thrds = new Thread[ThrCount];

        for (int i = 0; i < ThrCount; i++) {
            this.Thrds[i] = new Thread(() -> {
                while (!TermFlag) {
                    Consumer<Void> D = null;
                    synchronized (Q)
                    {
                        if (!Q.isEmpty()) {
                            D = Q.pop();
                        }
                    }
                    if (D != null)
                    {
                        D.accept(null);
                        synchronized (Q) {
                            Q.notifyAll();
                        }

                    }else{

                        try {
                            synchronized (Q)
                            {
                                Q.wait();
                            }
                        } catch (InterruptedException ignored) {
                            return;
                        }
                    }
                }
            });
            this.Thrds[i].start();
        }
    }
    @Override
    public void close() throws InterruptedException {
        TermFlag = true;
        for (Thread i : Thrds) {
            i.interrupt();
            i.join();
        }
    }
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> Args) throws InterruptedException {

        if (TermFlag) throw new IllegalStateException("ParallelMapperImpl was already closed");
        AtomicInteger Counter = new AtomicInteger(0);
        final int SIzeA = Args.size();
        ArrayList<R> ResList = new ArrayList<>(Args.size());
        int i = 0;
        while (i<SIzeA) {ResList.add(null); i++;}
        i = 0;
        while (i<SIzeA)
        {
            final int ind = i;
            synchronized (Q) {
                Q.push((whatever) ->
                {
                    T El;
                    El = Args.get(ind);
                    R res = f.apply(El);
                    synchronized (ResList)
                    {
                        ResList.set(ind, res);
                    }
                    Counter.incrementAndGet();
                    synchronized (Q) {
                        Q.notifyAll();
                    }
                });
            }
            i++;
        }
        synchronized (Q)
        {
            Q.notifyAll();
            while (Counter.get() < SIzeA)
            {
                Q.wait();
            }
        }
        return ResList;
    }
}
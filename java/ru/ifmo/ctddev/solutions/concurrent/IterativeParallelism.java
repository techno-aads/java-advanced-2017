package ru.ifmo.ctddev.solutions.concurrent;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;

public class IterativeParallelism implements ScalarIP
{

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException
    {
        Thread[] threadPool = new Thread[threads];
        List<T> maximums = new ArrayList<>();
        int countThread = Integer.min(threads, values.size());
        int sizeSubList = values.size() / countThread;

        for (int i = 0; i < countThread; i++)
        {
            maximums.add(null);
        }

        for (int i = 0; i < countThread; i++)
        {
            int localID = i;

            Thread thread = new Thread(() -> {
                int startIndex = sizeSubList * localID;
                int endIndex;

                if(localID == countThread - 1)
                {
                    endIndex = values.size();
                }
                else
                {
                    endIndex = startIndex + sizeSubList;
                }

                T localMax = Collections.max(values.subList(startIndex, endIndex), comparator);

                maximums.set(localID, localMax);
            });

            threadPool[i] = thread;
            thread.start();
        }

        for (int i = 0; i < countThread; i++)
        {
            threadPool[i].join();
        }

        return Collections.max(maximums, comparator);
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException
    {
        return maximum(threads, values, Collections.reverseOrder(comparator));
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException
    {
        Thread[] threadPool = new Thread[threads];
        int countThread = Integer.min(threads, values.size());
        int sizeSubList = values.size() / countThread;
        Boolean[] res = new Boolean[1];
        res[0] = true;

        for (int i = 0; i < countThread; i++)
        {
            int localI = i;
            Thread thread = new Thread(() -> {

                int startIndex = sizeSubList * localI;

                int endIndex;
                if(localI == countThread - 1)
                {
                    endIndex = values.size();
                }
                else
                {
                    endIndex = startIndex + sizeSubList;
                }

                if (startIndex < endIndex)
                {
                    boolean localResult = values.subList(startIndex, endIndex).stream().allMatch(predicate);
                    synchronized (res) {
                        res[0] &= localResult;
                    }
                }
            });

            threadPool[i] = thread;
            thread.start();
        }

        for (int i = 0; i < countThread; i++)
        {
            threadPool[i].join();
        }

        return res[0];
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException
    {
        return !all(threads, values, predicate.negate());
    }
}
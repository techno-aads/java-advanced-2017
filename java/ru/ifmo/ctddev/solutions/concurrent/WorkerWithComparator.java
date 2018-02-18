package ru.ifmo.ctddev.solutions.concurrent;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class WorkerWithComparator<T> implements Runnable {
    enum ComparatorType {
        MIN,
        MAX
    }

    private List<? extends T> values;
    private Comparator<? super T> comparator;
    private ComparatorType type;
//    private T result;
    private ArrayBlockingQueue<? super T> toWriteRes;

    public WorkerWithComparator(List<? extends T> values, Comparator<? super T> comparator, ComparatorType type
        , ArrayBlockingQueue<? super T> toWriteRes) {
        this.comparator = comparator;
        this.values = values;
        this.type = type;
        this.toWriteRes = toWriteRes;
    }

    @Override
    public void run() {
        switch (type) {
            case MAX:
                if(!values.isEmpty())
                    toWriteRes.add(Collections.max(values, comparator));
                break;
            case MIN:
                if(!values.isEmpty())
                    toWriteRes.add(Collections.min(values, comparator));
                break;
        }
    }

//    public T getResult() {
//        return result;
//    }
}
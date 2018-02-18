package ru.ifmo.ctddev.solutions.concurrent;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Predicate;

public class WorkerWithPredicate<T> implements Runnable {
    enum PredicateType {
        ALL,
        ANY
    }

    private List<? extends T> values;
    private Predicate<? super T> predicate;
    private PredicateType type;
    private ArrayBlockingQueue<Boolean> toWriteRes;

    public WorkerWithPredicate(List<? extends T> values, Predicate<? super T> predicate, PredicateType type
            , ArrayBlockingQueue<Boolean> toWriteRes) {
        this.values = values;
        this.predicate = predicate;
        this.type = type;
        this.toWriteRes = toWriteRes;
    }

    @Override
    public void run() {
        switch (type) {
            case ALL:
                if(!values.isEmpty())
                    toWriteRes.add(all(values, predicate));
                break;
            case ANY:
                if(!values.isEmpty())
                    toWriteRes.add(any(values, predicate));
                break;
        }
    }

    private Boolean all(List<? extends T> values, Predicate<? super T> predicate) {
        for(T val : values) {
            if(!predicate.test(val))
                return false;
        }
        return true;
    }

    private Boolean any(List<? extends T> values, Predicate<? super T> predicate) {
        return !all(values, predicate.negate());
    }
}

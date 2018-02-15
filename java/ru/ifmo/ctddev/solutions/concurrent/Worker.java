package ru.ifmo.ctddev.solutions.concurrent;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class Worker<T> implements Runnable {

    public enum OpType {
        MIN,
        MAX,
        ALL,
        ANY
    }

    private Comparator<? super T> comparator;
    private List<? extends T> values;
    private Predicate<? super T> predicate;
    private OpType type;
    private T result1;
    private boolean result2;

    public Worker(List<? extends T> values, Comparator<? super T> comparator, OpType type) {
        this.comparator = comparator;
        this.values = values;
        this.type = type;
    }

    public Worker(List<? extends T> values, Predicate<? super T> predicate, OpType type) {
        this.values = values;
        this.predicate = predicate;
        this.type = type;
    }

    @Override
    public void run() {
//        switch (type) {
//            case MAX:
//                result = (E) Collections.max(values, comparator);
//                break;
//            case MIN:
//                result = (E) Collections.min(values, comparator);
//                break;
//            case ALL:
//                result = (E) all(values, predicate);
//        }
    }

    private boolean all(List<? extends T> values, Predicate<? super T> predicate) {
        for(T val : values) {
            if(!predicate.test(val))
                return false;
        }
        return true;
    }

    private boolean any(List<? extends T> values, Predicate<? super T> predicate) {
        return !all(values, predicate.negate());
    }
}

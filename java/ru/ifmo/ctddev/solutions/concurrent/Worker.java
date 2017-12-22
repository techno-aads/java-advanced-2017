package ru.ifmo.ctddev.solutions.concurrent;

import java.util.List;
import java.util.function.Function;

public class Worker<T, R> implements Runnable {
    private final Function<List<T>, R> f;
    private final List<T> args;
    private R result;

    Worker(Function<List<T>, R> f, List<T> args) {
        this.f = f;
        this.args = args;
    }

    @Override
    public void run() {
        result = f.apply(args);
    }

    public R getResult(){
        return result;
    }
}

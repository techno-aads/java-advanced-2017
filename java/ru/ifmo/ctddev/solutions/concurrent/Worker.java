package ru.ifmo.ctddev.solutions.concurrent;
import java.util.List;
import java.util.function.Function;

public class Worker<T, R> implements Runnable {
    private final Function<List<T>, R> Funct;
    private final List<T> Args;
    private R Res;

    Worker(Function<List<T>, R> Funct, List<T> Args)
    {
        this.Funct = Funct;
        this.Args = Args;
    }

    @Override
    public void run() {
        Res = Funct.apply(Args);
    }

    public R getResult(){
        return Res;
    }
}

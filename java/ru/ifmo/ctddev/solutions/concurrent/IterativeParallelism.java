package ru.ifmo.ctddev.solutions.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class IterativeParallelism implements ListIP {

    private final ParallelMapper Map;
    public IterativeParallelism() {
        this.Map = null;
    }
    public IterativeParallelism(ParallelMapper Map) {
        this.Map = Map;
    }
    private <T> Stream<T> toStream(List<T> Args) {
        return Args.stream();
    }
    private <T, R> R makeParallel(int ThCount, List<T> Args,Function<List<T>, R> f, Function<List<R>, R> Coll) throws InterruptedException
    {
        List<List<T>> Objs = split(Args, ThCount);
        List<R> Res = new ArrayList<>();
        if (Map == null) {
            List<Worker<T, R>> Wkrs = new ArrayList<>();
            Objs.forEach(Obj -> Wkrs.add(new Worker<>(f, Obj)));
            List<Thread> Thrds = new ArrayList<>();
            for (Worker<T, R> Wkr : Wkrs) {
                Thread t = new Thread(Wkr);
                Thrds.add(t);
                t.start();
            }
            for (Thread t : Thrds) {
                t.join();
            }
            for (Worker<T, R> Wkr : Wkrs) {
                Res.add(Wkr.getResult());
            }
        } else {
            Res = Map.map(f, Objs);
        }
        return Coll.apply(Res);
    }
    private <T> List<List<T>> split(List<T> Args, int ChunckC) {
        List<List<T>> Res = new ArrayList<>();
        int P = Args.size() / ChunckC;
        int Mod = Args.size() % ChunckC;
        int Start = 0;
        int Finnish;
        int i = 0;
        while (i != ChunckC)
        {
            if (Start == Args.size()) {
                break;
            }
            Finnish = Math.min(Start + P, Args.size()) + (Mod-- > 0 ? 1 : 0);
            Res.add(Args.subList(Start, Finnish));
            Start = Finnish;
            i++;
        }
        return Res;
    }

    @Override
    public <T, U> List<U> map(int Thrds, List<? extends T> Val, Function<? super T, ? extends U> f) throws InterruptedException {
        return makeParallel(Thrds, Val,Obj -> toStream(Obj).map(f).collect(Collectors.toList()),Res -> toStream(Res).map(List::stream)
                .reduce(Stream::concat).orElse(null).collect(Collectors.toList()));
    }
    @Override
    public <T> List<T> filter(int Thrds, List<? extends T> Val, Predicate<? super T> Pred) throws InterruptedException {
        return makeParallel(Thrds, Val,Obj -> toStream(Obj).filter(Pred).collect(Collectors.toList()),Res -> toStream(Res).reduce((r1, r2) -> Stream.concat(
                toStream(r1), toStream(r2)).collect(Collectors.toList())).orElse(null));
    }
    @Override
    public String join(int Thrds, List<?> Val) throws InterruptedException {
        return makeParallel(Thrds, Val,Obj -> toStream(Obj).map(Object::toString).reduce(String::concat).orElse(null),
                Res -> toStream(Res).reduce(String::concat).orElse(null));
    }
    @Override
    public <T> boolean any(int Thrds, List<? extends T> Val, Predicate<? super T> Pred) throws InterruptedException {
        return !all(Thrds, Val, Pred.negate());
    }
    @Override
    public <T> T maximum(int Thrds, List<? extends T> Val, Comparator<? super T> Comp) throws InterruptedException {
        return makeParallel(Thrds, Val, Obj -> Collections.max(Obj, Comp),Res -> Collections.max(Res, Comp));
    }
    @Override
    public <T> T minimum(int Thrds, List<? extends T> Val, Comparator<? super T> Comp) throws InterruptedException {
        return maximum(Thrds, Val, Collections.reverseOrder(Comp));
    }
    @Override
    public <T> boolean all(int Thrds, List<? extends T> Val, Predicate<? super T> Pred) throws InterruptedException {
        return makeParallel(Thrds, Val,Obj -> toStream(Obj).allMatch(Pred),Res -> toStream(Res).allMatch(x -> x));
    }
}

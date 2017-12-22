package ru.ifmo.ctddev.solutions.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class IterativeParallelism implements ListIP {

    private final ParallelMapper mapper;

    public IterativeParallelism() {
        this.mapper = null;
    }
    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    private <T> Stream<T> toStream(List<T> args) {
        return args.stream();
    }

    private <T> List<List<T>> split(List<T> args, int chunkCount) {
        List<List<T>> result = new ArrayList<>();

        int pack = args.size() / chunkCount;
        int mod = args.size() % chunkCount;
        int start = 0;
        int end;


        for (int i = 0; i < chunkCount; i++) {
            if (start == args.size()) {
                break;
            }
            end = Math.min(start + pack, args.size()) + (mod-- > 0 ? 1 : 0);
            //end = start + pack + (mod-- > 0 ? 1 : 0);
            result.add(args.subList(start, end));
            start = end;
        }

        return result;
    }

    private <T, R> R makeParallel(int threadCount, List<T> args,
                                  Function<List<T>, R> f,
                                  Function<List<R>, R> collector) throws InterruptedException {

        List<List<T>> tasks = split(args, threadCount);
        List<R> results = new ArrayList<>();

        if (mapper == null) {
            List<Worker<T, R>> workers = new ArrayList<>();
            tasks.forEach(task -> workers.add(new Worker<>(f, task)));

            List<Thread> threads = new ArrayList<>();
            for (Worker<T, R> worker : workers) {
                Thread t = new Thread(worker);
                threads.add(t);
                t.start();
            }
            for (Thread t : threads) {
                t.join();
            }

            for (Worker<T, R> worker : workers) {
                results.add(worker.getResult());
            }
        } else {
            results = mapper.map(f, tasks);
        }

        return collector.apply(results);
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return makeParallel(threads, values,
                task -> Collections.max(task, comparator),
                result -> Collections.max(result, comparator));
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, Collections.reverseOrder(comparator));
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return makeParallel(threads, values,
                task -> toStream(task).allMatch(predicate),
                result -> toStream(result).allMatch(x -> x));
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return makeParallel(threads, values,
                task -> toStream(task).map(Object::toString).reduce(String::concat).orElse(null),
                result -> toStream(result).reduce(String::concat).orElse(null));



    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return makeParallel(threads, values,
                task -> toStream(task).filter(predicate).collect(Collectors.toList()),
                result -> toStream(result).reduce((r1, r2) -> Stream.concat(
                        toStream(r1), toStream(r2)).collect(Collectors.toList())).orElse(null));
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return makeParallel(threads, values,
                task -> toStream(task).map(f).collect(Collectors.toList()),
                result -> toStream(result).map(List::stream)
                        .reduce(Stream::concat).orElse(null).collect(Collectors.toList()));
    }

}
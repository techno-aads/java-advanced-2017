package ru.ifmo.ctddev.razzhivin.hw67;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author zakhar.razzhivin
 */
public class IterativeParallelism implements ListIP {

    private ParallelMapper mapper;

    public IterativeParallelism() {
    }

    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public <T> T minimum(int threadsNum, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return parallelListProcessing(
                threadsNum,
                list,
                chunk -> Collections.min(chunk, comparator),
                results -> Collections.min(results, comparator)
        );
    }

    @Override
    public <T> T maximum(int threadsNum, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return parallelListProcessing(
                threadsNum,
                list,
                chunk -> Collections.max(chunk, comparator),
                results -> Collections.max(results, comparator)
        );
    }

    @Override
    public <T> boolean all(int threadsNum, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return parallelListProcessing(
                threadsNum,
                list,
                chunk -> chunk.stream().allMatch(predicate),
                results -> results.stream().allMatch(Predicate.isEqual(true))
        );
    }

    @Override
    public <T> boolean any(int threadsNum, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return parallelListProcessing(
                threadsNum,
                list,
                chunk -> chunk.stream().anyMatch(predicate),
                results -> results.stream().anyMatch(Predicate.isEqual(true))
        );
    }

    @Override
    public String join(int threadsNum, List<?> list) throws InterruptedException {
        StringBuilder result = new StringBuilder();
        map(threadsNum, list, Object::toString).forEach(result::append);
        return result.toString();
    }

    @Override
    public <T> List<T> filter(int threadsNum, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return parallelListProcessing(
                threadsNum,
                list,
                chunk -> chunk.stream().filter(predicate).collect(Collectors.toList()),
                results -> results.stream().collect(ArrayList::new, List::addAll, List::addAll)
        );
    }

    @Override
    public <T, U> List<U> map(int threadsNum, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        return parallelListProcessing(
                threadsNum,
                list,
                chunk -> chunk.stream().map(function).collect(Collectors.toList()),
                results -> results.stream().collect(ArrayList::new, List::addAll, List::addAll)
        );
    }

    private static class ChunkWorker<T, R> implements Runnable {

        private List<? extends T> chunk;
        private Function<List<? extends T>, R> action;
        private R result;

        private ChunkWorker(List<? extends T> chunk, Function<List<? extends T>, R> action) {
            this.chunk = chunk;
            this.action = action;
        }

        public R getResult() {
            return result;
        }

        @Override
        public void run() {
            result = action.apply(chunk);
        }
    }

    private <T> List<List<? extends T>> split(int chunksCount, List<? extends T> list) {
        if (chunksCount < 1) {
            chunksCount = 1;
        } else if (chunksCount > list.size()) {
            chunksCount = list.size();
        }
        int chunkSize = (int) Math.ceil(1d * list.size() / chunksCount);
        int lowerBound = 0;
        int upperBound = Math.min(lowerBound + chunkSize, list.size());
        List<List<? extends T>> chunks = new ArrayList<>();
        for (int i = 0; i < chunksCount; i++) {
            chunks.add(list.subList(lowerBound, upperBound));
            chunkSize = (int) Math.ceil(1d * (list.size() - upperBound) / (chunksCount - i - 1));
            lowerBound = upperBound;
            upperBound = Math.min(list.size(), upperBound + chunkSize);
        }
        return chunks;
    }

    private <T, R> R parallelListProcessing(int threadsNum, List<? extends T> list,
                                            Function<List<? extends T>, R> actionOverChunk,
                                            Function<List<R>, R> combinerAction) throws InterruptedException {
        List<List<? extends T>> chunks = split(threadsNum, list);
        if (mapper != null) {
            List<R> results = mapper.map(actionOverChunk, chunks);
            return combinerAction.apply(results);
        }

        List<Thread> threads = new ArrayList<>();
        List<ChunkWorker<T, R>> workers = new ArrayList<>();

        for (List<? extends T> chunk : chunks) {
            ChunkWorker<T, R> worker = new ChunkWorker<>(chunk, actionOverChunk);
            workers.add(worker);
            Thread thread = new Thread(worker);
            threads.add(thread);
            thread.start();
        }

        for (Thread room : threads) {
            room.join();
        }

        return combinerAction.apply(workers.stream().map(ChunkWorker::getResult).collect(Collectors.toList()));
    }
}


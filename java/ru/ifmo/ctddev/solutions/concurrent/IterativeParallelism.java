package ru.ifmo.ctddev.solutions.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Nikita Sokeran
 */
public class IterativeParallelism implements ListIP {
    private final ParallelMapper mapper;

    public IterativeParallelism() {
        this.mapper = null;
    }

    public IterativeParallelism(final ParallelMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public <T> T maximum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator) {
        return executeParallel(
                threads,
                values,
                vals -> Arrays.asList(Collections.max(vals, comparator)),
                vals -> Collections.max(vals, comparator)
        );
    }

    @Override
    public <T> T minimum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator) {
        return executeParallel(
                threads,
                values,
                vals -> Arrays.asList(Collections.min(vals, comparator)),
                vals -> Collections.min(vals, comparator)
        );
    }

    @Override
    public <T> boolean all(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) {
        return executeParallel(
                threads,
                values,
                vals -> Arrays.asList(vals.stream().allMatch(predicate)),
                vals -> vals.stream().allMatch(Boolean.TRUE::equals)
        );
    }

    @Override
    public <T> boolean any(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) {
        return executeParallel(
                threads,
                values,
                vals -> Arrays.asList(vals.stream().anyMatch(predicate)),
                vals -> vals.stream().anyMatch(Boolean.TRUE::equals)
        );
    }

    @Override
    public String join(final int threads, final List<?> values) {
        return executeParallel(
                threads,
                values,
                vals -> Arrays.asList(
                        vals.stream().map(Object::toString).collect(Collectors.joining(""))
                ),
                vals -> String.join("", vals)
        );
    }

    @Override
    public <T> List<T> filter(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) {
        return executeParallel(
                threads,
                values,
                vals -> vals.stream().filter(predicate).collect(Collectors.toList()),
                Function.identity()
        );
    }

    @Override
    public <T, R> List<R> map(final int threads, final List<? extends T> values, final Function<? super T, ? extends R> function) {
        return executeParallel(
                threads,
                values,
                vals -> vals.stream().map(function).collect(Collectors.toList()),
                Function.identity()
        );
    }

    private <L, M, R> R executeParallel(final int threads, final List<L> list,
                                        final Function<List<L>, List<M>> function,
                                        final Function<List<M>, R> merger) {
        if (mapper == null) {
            final Collection<ParallelTask<List<M>>> tasks = CollectionUtils.createBatchesForParallelExecution(list, threads).stream()
                    .map(batch -> ParallelTask.started(() -> function.apply(batch)))
                    .collect(Collectors.toList());

            return merger.apply(
                    tasks.stream()
                            .flatMap(task -> task.awaitResult().stream())
                            .collect(Collectors.toList())
            );
        }

        final List<List<L>> batches = CollectionUtils.createBatchesForParallelExecution(list, threads);
        final List<List<M>> result;
        try {
            result = mapper.map(function, batches);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Couldn't apply mapper's function.", e);
        }

        return merger.apply(result.stream().flatMap(Collection::stream).collect(Collectors.toList()));
    }
}
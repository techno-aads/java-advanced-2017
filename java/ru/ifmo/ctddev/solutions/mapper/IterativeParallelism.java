package ru.ifmo.ctddev.solutions.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

public class IterativeParallelism extends ru.ifmo.ctddev.solutions.concurrent.IterativeParallelism {
    private ParallelMapper parallelMapper = null;

    @SuppressWarnings("unused")
    public IterativeParallelism() {super();}

    @SuppressWarnings("unused")
    public IterativeParallelism(ParallelMapper parallelMapper) {
        super();
        this.parallelMapper = parallelMapper;
    }

    @Override
    protected  <T, R> List<R> execute(int n, List<? extends T> data, Function<List<T>, ? extends R> function) throws InterruptedException {
        return Objects.nonNull(parallelMapper) ? parallelMapper.map(function, cast(split(data, n))) : super.execute(n, data, function);
    }

    @SuppressWarnings("unchecked")
    private <T> List<List<T>> cast(List<List<? extends T>> data) {
        List<List<T>> result = new ArrayList<>(data.size());
        data.forEach(v -> result.add((List<T>) v));
        return result;
    }
}

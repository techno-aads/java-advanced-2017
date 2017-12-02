package ru.ifmo.ctddev.solutions.mapper;

import java.util.List;
import java.util.function.Function;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

public class ParallelMapperImpl implements ParallelMapper {

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        //todo
        return null;
    }

    @Override
    public void close() throws InterruptedException {
        //todo
    }
}

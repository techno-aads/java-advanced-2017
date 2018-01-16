package ru.ifmo.ctddev.solutions.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Mikhail Yandimirov on 01.01.2018.
 */
public class ParallelMapperImpl implements ParallelMapper {

  private final ExecutorService executorService;

  public ParallelMapperImpl(int threads) {
    executorService = Executors.newFixedThreadPool(threads);
  }

  @Override
  public <T, R> List<R> map(Function<? super T, ? extends R> function, List<? extends T> list) throws InterruptedException {
    List<Future<? extends R>> futures = list.stream()
        .map(arg -> (Callable<? extends R>) () -> function.apply(arg))
        .map(executorService::submit)
        .collect(Collectors.toList());

    List<R> results = new ArrayList<>();
    for (Future<? extends R> future : futures) {
      try {
        results.add(future.get());
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
    }

    return results;
  }

  @Override
  public void close() throws InterruptedException {
    executorService.shutdown();
    try {
      if (!executorService.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
        executorService.shutdownNow();
      }
    } catch (InterruptedException e) {
      executorService.shutdownNow();
    }
  }
}

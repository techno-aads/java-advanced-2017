package ru.ifmo.ctddev.solutions.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Mikhail Yandimirov on 09.12.2017.
 */
public class ThreadExecutor {

  private static final int MAX_THREADS = Runtime.getRuntime().availableProcessors();

  public static <T, A> List<A> execute(final int threads, final List<T> list, final Function<List<T>, List<A>> function) {
    final List<Task<T, A>> taskList = new ArrayList<>();
    final int batchSize = getBatchSize(threads, list.size());
    for (int left = 0; left < list.size(); left += batchSize) {
      final Task<T, A> thread = new Task<>(list.subList(left, Math.min(left + batchSize, list.size())), function);
      taskList.add(thread);
      thread.start();
    }
    waitAllTasks(taskList);
    return taskList.stream()
        .flatMap(thread -> thread.getResult().stream())
        .collect(Collectors.toList());
  }

  private static <T, A> void waitAllTasks(final List<Task<T, A>> tasks) {
    for (Task task : tasks) {
      try {
        task.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private static int getBatchSize(final int threads, final int listSize) {
    final int actualThreads = Math.min(Math.min(MAX_THREADS, threads), listSize);
    return listSize / actualThreads;
  }
}

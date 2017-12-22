package ru.ifmo.ctddev.solutions.concurrent;

import java.util.List;
import java.util.function.Function;

/**
 * Created by Mikhail Yandimirov on 07.12.2017.
 */
public class Task<T, A> extends Thread{

  private Function<List<T>, List<A>> function;
  private List<A> result;
  private List<T> source;

  public Task(final List<T> source, final Function<List<T>, List<A>> function) {
    this.source = source;
    this.function = function;
  }

  @Override
  public void run() {
    result = function.apply(source);
  }

  public List<A> getResult() {
    return result;
  }
}

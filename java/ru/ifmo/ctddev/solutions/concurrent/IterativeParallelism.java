package ru.ifmo.ctddev.solutions.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Mikhail Yandimirov on 07.12.2017.
 */
public class IterativeParallelism implements ListIP {

  public <T> T minimum(final int threads, final  List<? extends T> list, final  Comparator<? super T> comparator) {
    return applyFunction(
        threads,
        list,
        values -> Collections.singletonList(Collections.min(values, comparator)),
        values -> Collections.min(values, comparator));
  }

  public <T> T maximum(final int threads, final List<? extends T> list, final Comparator<? super T> comparator) {
    return applyFunction(
        threads,
        list,
        values -> Collections.singletonList(Collections.max(values, comparator)),
        values -> Collections.max(values, comparator));
  }

  public <T> boolean all(final int threads, final List<? extends T> list, final Predicate<? super T> predicate) {
    return applyFunction(
        threads,
        list,
        values -> Collections.singletonList(values.stream()
            .allMatch(predicate)),
        values -> values.stream()
            .allMatch(e -> e.equals(true)));
  }

  public <T> boolean any(final int threads, final List<? extends T> list, final Predicate<? super T> predicate) {
    return applyFunction(
        threads,
        list,
        values -> Collections.singletonList(values.stream()
            .anyMatch(predicate)),
        values -> values.stream()
            .anyMatch(e -> e.equals(true)));
  }


  public <T, R> List<R> map(final int threads, final List<? extends T> list, final Function<? super T, ? extends R> function) {
    return applyFunction(
        threads,
        list,
        values -> values.stream()
            .map(function)
            .collect(Collectors.toList()),
        Function.identity());
  }

  public <T> List<T> filter(final int threads, final List<? extends T> list, final Predicate<? super T> predicate) {
    return applyFunction(
        threads,
        list,
        values -> values.stream()
            .filter(predicate)
            .collect(Collectors.toList()),
        Function.identity());
  }

  public String join(final int threads, final List<?> list) {
    return applyFunction(
        threads,
        list,
        values -> Collections.singletonList(values.stream()
            .map(Object::toString)
            .collect(Collectors.joining(""))),
        values -> String.join("", values));
  }

  private <T, A, R> R applyFunction(final int threads, final List<T> list, final Function<List<T>, List<A>> function, final Function<List<A>, R> merger) {
    List<A> results = ThreadExecutor.execute(threads, list, function);
    return merger.apply(results);
  }
}
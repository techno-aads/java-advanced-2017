package ru.ifmo.ctddev.solutions.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A class providing a couple of iterative-parallel methods.
 */
public class IterativeParallelism implements ListIP {
    private static <E, T, U> T parallelProcess(int threads, final List<? extends E> list,
                                               Function<List<? extends E>, U> f,
                                               Function<? super List<? extends U>, ? extends T> accumulate)
            throws InterruptedException {
        if (threads <= 0) {
            return null;
        }
        int listLen = list.size();
        final int threadsNum = Math.min(listLen, threads);
        Thread[] threadsArray = new Thread[threadsNum];
        @SuppressWarnings("unchecked")
        final U[] results = (U[]) new Object[threadsNum];
        for (int i = 0; i < threadsNum; i++) {
            final int fi = i;
            final List<? extends E> subList = list.subList(fi * listLen / threadsNum, (fi + 1) * listLen / threadsNum);
            threadsArray[i] = new Thread(() -> {
                results[fi] = f.apply(subList);
            });
            threadsArray[i].start();
        }
        for (Thread thread : threadsArray) {
            thread.join();
        }
        List<U> resultsList = Arrays.asList(results);
        return accumulate.apply(resultsList);
    }

    private static <E, R> List<R> map1(List<? extends E> list, Function<? super E, ? extends R> f) {
        return list.stream().map(f::apply).collect(Collectors.toList());
    }

    private static <E> E fold1(List<? extends E> list, BinaryOperator<E> f) {
        Iterator<? extends E> iterator = list.iterator();
        if (!iterator.hasNext()) {
            return null;
        }
        E e = iterator.next();
        while (iterator.hasNext()) {
            e = f.apply(e, iterator.next());
        }
        return e;
    }

    private static <E> List<E> filter1(List<? extends E> list, Predicate<? super E> predicate) {
        return list.stream().filter(predicate).collect(Collectors.toList());
    }

    private static <E> E fold(int threads, List<? extends E> list, BinaryOperator<E> f)
            throws InterruptedException {
        return parallelProcess(threads, list, l -> fold1(l, f), l -> fold1(l, f));
    }

    /**
     * Concurrently computes the minimum on the given list with the given comparator.
     *
     * @param threads    number of threads allowed to use
     * @param list       the list to compute the minimum on
     * @param comparator the comparator to compare the elements with
     * @param <E>        the type of the elements of the given list
     * @return the first minimum on the given list
     * @throws InterruptedException when one of the threads was interrupted
     */
    @Override
    public <E> E minimum(int threads, List<? extends E> list, Comparator<? super E> comparator) throws InterruptedException {
        return fold(threads, list, (e, e2) -> (comparator.compare(e, e2) <= 0 ? e : e2));
    }

    /**
     * Concurrently computes the maximum on the given list with the given comparator.
     *
     * @param threads    number of threads allowed to use
     * @param list       the list to compute the maximum on
     * @param comparator the comparator to compare the elements with
     * @param <E>        the type of the elements of the given list
     * @return the first maximum on the given list
     * @throws InterruptedException when one of the threads was interrupted
     */
    @Override
    public <E> E maximum(int threads, List<? extends E> list, Comparator<? super E> comparator) throws InterruptedException {
        return minimum(threads, list, (x, y) -> comparator.compare(y, x));
    }


    /**
     * Concurrently checks whether all of the list elements comply with the predicate
     *
     * @param threads   number of threads allowed to use
     * @param list      the list to check
     * @param predicate the predicate
     * @param <E>       the type of the elements of the list
     * @return the answer
     * @throws InterruptedException when one of the threads was interrupted
     */
    @Override
    public <E> boolean all(int threads, List<? extends E> list, Predicate<? super E> predicate) throws InterruptedException {
        return fold(threads, map(threads, list, predicate::test), Boolean::logicalAnd);
    }

    /**
     * Concurrently checks whether any of the list elements comply with the predicate
     *
     * @param threads   number of threads allowed to use
     * @param list      the list to check
     * @param predicate the predicate
     * @param <E>       the type of the elements of the list
     * @return the answer
     * @throws InterruptedException when one of the threads was interrupted
     */
    @Override
    public <E> boolean any(int threads, List<? extends E> list, Predicate<? super E> predicate) throws InterruptedException {
        return !all(threads, list, predicate.negate());
    }

    /**
     * Concatenates the {@link String} representations of the list elements
     *
     * @param threads number of threads allowed to use
     * @param list    the list to concatenate
     * @return a {@link String}, containing the concatenation
     * @throws InterruptedException when one of the threads was interrupted
     */
//    @Override
    public String join(int threads, final List<?> list) throws InterruptedException {
        return fold(threads, map(threads, list, Object::toString), (a, b) -> a += b);
    }

    /**
     * Concurrently filters the given list with the given predicate
     *
     * @param threads   number of threads allowed to use
     * @param list      the list to filter
     * @param predicate the predicate to filter with
     * @param <E>       the type of elements in the given list
     * @return the list containing all of the elements of the given list, удовлетворяющие the given predicate,
     * in the same order as they were in the given list.
     * @throws InterruptedException when one of the threads was interrupted
     */
    @Override
    public <E> List<E> filter(int threads, final List<? extends E> list, Predicate<? super E> predicate) throws InterruptedException {
        return parallelProcess(threads, list, list1 -> filter1(list1, predicate),
                lists -> fold1(lists, (a, b) -> {
                    a.addAll(b);
                    return a;
                }));
    }

    /**
     * Concurrently maps the given list with the given function.
     *
     * @param threads number of threads allowed to use
     * @param list    the list to map
     * @param f       the function to map with
     * @param <E>     the type of elements in the given list
     * @param <R>     the type of return value of the function
     * @return the mapped list
     * @throws InterruptedException when one of the threads was interrupted
     */
    @Override
    public <E, R> List<R> map(int threads, List<? extends E> list, Function<? super E, ? extends R> f)
            throws InterruptedException {
        return parallelProcess(threads, list, list1 -> map1(list1, f), lists -> fold1(lists, (a, b) -> {
            a.addAll(b);
            return a;
        }));
    }
}
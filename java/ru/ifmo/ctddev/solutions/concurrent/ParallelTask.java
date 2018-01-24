package ru.ifmo.ctddev.solutions.concurrent;

import java.util.concurrent.Callable;

/**
 * @author Nikita Sokeran
 */
public final class ParallelTask<T> {
    private final Thread thread;

    private T result;

    private ParallelTask(final Callable<T> callable) {
        this.thread = new Thread(() -> {
            try {
                this.result = callable.call();
            } catch (Exception e) {
                throw new IllegalStateException("Error occurred during task execution.", e);
            }
        });
    }

    public static <T> ParallelTask<T> started(final Callable<T> callable) {
        final ParallelTask<T> task = new ParallelTask<>(callable);
        task.start();
        return task;
    }

    public T awaitResult() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new IllegalStateException("Task has been interrupted.", e);
        }
        return result;
    }

    private void start() {
        thread.start();
    }
}

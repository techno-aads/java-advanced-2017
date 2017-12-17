package ru.ifmo.ctddev.razzhivin.hw67;

import java.util.LinkedList;
import java.util.function.Function;

/**
 * @author zakhar.razzhivin
 */
public class Queue {

    public static class Task<T, R> {

        private T argument;
        private Function<? super T, ? extends R> function;
        private R result;

        public Task(T argument, Function<? super T, ? extends R> function) {
            this.argument = argument;
            this.function = function;
        }

        public synchronized void process() {
            result = function.apply(argument);
            notifyAll();
        }

        public synchronized R getResult() throws InterruptedException {
            while (result == null) {
                wait();
            }
            return result;
        }
    }

    private final java.util.Queue<Task<?, ?>> queue;

    public Queue() {
        this.queue = new LinkedList<>();
    }

    public synchronized void addTask(Task<?, ?> task) {
        queue.add(task);
        notifyAll();
    }

    public synchronized Task<?, ?> getNextTask() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        return queue.poll();
    }
}

package ru.ifmo.ctddev.solutions.concurrent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ThreadsPull {

    private final List<Thread> m_Threads;

    private ThreadsPull(List<Thread> threads) {
        this.m_Threads = Collections.unmodifiableList(threads);
    }

    public void join() throws InterruptedException {
        for (Thread thread : m_Threads) {
            thread.join();
        }
    }

    public static class Builder {

        private final List<Thread> m_Threads = new ArrayList<>();

        public Builder add(Thread thread) {
            m_Threads.add(thread);
            return this;
        }

        public ThreadsPull build() {
            return new ThreadsPull(m_Threads);
        }
    }
}
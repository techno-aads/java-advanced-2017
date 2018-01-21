package ru.ifmo.ctddev.solutions.concurrent;

abstract class Callable<T> implements Runnable {

    private T m_Response = null;

    T getResponse() {
        return m_Response;
    }
    void setResponse(T response) {
        m_Response = response;
    }
}
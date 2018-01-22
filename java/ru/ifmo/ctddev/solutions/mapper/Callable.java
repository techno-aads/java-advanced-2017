package ru.ifmo.ctddev.solutions.mapper;

abstract class Callable<T> implements Runnable {

    private T response = null;

    T getResponse() {
        return response;
    }

    void setResponse(T response) {
        this.response = response;
    }
}

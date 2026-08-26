package com.example.Exception;

public class NoInitOrDestoryMethodException extends RuntimeException {
    public NoInitOrDestoryMethodException(String message) {
        super(message);
    }
}

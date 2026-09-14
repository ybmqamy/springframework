package com.example.Exception;

public class ServerErrorException extends RuntimeException {
    public ServerErrorException(String message) {
        super(message);
    }
}

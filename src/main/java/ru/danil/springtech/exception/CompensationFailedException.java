package ru.danil.springtech.exception;

public class CompensationFailedException extends RuntimeException {
    public CompensationFailedException(String message) {
        super(message);
    }
}

package ru.danil.springtech.exception;

public class PolicyCreationException extends RuntimeException {
    public PolicyCreationException(String message) {
        super(message);
    }
}

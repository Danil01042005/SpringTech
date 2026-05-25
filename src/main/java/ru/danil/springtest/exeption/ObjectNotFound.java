package ru.danil.springtest.exeption;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ObjectNotFound extends RuntimeException {
    public ObjectNotFound(String message) {
        super(message);
    }
}

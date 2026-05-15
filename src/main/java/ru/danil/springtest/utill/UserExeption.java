package ru.danil.springtest.utill;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public class UserExeption extends RuntimeException {

    private final HttpStatus status;

    public UserExeption(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}

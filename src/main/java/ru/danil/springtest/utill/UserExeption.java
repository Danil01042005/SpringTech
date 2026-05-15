package ru.danil.springtest.utill;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public class UserExeption extends RuntimeException {

    private final HttpStatus status;

    public UserExeption(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

//e164d112-ed28-4495-9556-ad9b6cddd0c1
//550e8400-e29b-41d4-a716-446655440000
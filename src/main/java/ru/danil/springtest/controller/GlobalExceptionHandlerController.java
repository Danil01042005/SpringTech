package ru.danil.springtest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.danil.springtest.dto.ErrorResponse;
import ru.danil.springtest.utill.ObjectNotFound;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandlerController {

    @ExceptionHandler(ObjectNotFound.class)
    public ResponseEntity<ErrorResponse> handleUserException(ObjectNotFound e) {
        return new ResponseEntity<>(
                new ErrorResponse().message(e.getMessage()).code(e.getStatus().value()),
                e.getStatus()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return handleUserException(new ObjectNotFound(message, HttpStatus.BAD_REQUEST));
    }

}
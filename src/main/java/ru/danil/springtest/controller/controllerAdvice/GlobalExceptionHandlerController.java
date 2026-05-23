package ru.danil.springtest.controller.controllerAdvice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.danil.springtest.dto.ErrorResponse;
import ru.danil.springtest.utill.ObjectNotFound;

@RestControllerAdvice
public class GlobalExceptionHandlerController {

    @ExceptionHandler(ObjectNotFound.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserException(ObjectNotFound e) {
        return new ErrorResponse().message(e.getMessage()).code(HttpStatus.NOT_FOUND.value());
    }
}
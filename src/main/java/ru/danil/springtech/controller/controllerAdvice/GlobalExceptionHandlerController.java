package ru.danil.springtech.controller.controllerAdvice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.danil.springtech.dto.ErrorResponse;
import ru.danil.springtech.exception.ObjectNotFoundException;

import ru.danil.springtech.exception.ServiceUnavailableException;

@RestControllerAdvice
public class GlobalExceptionHandlerController {

    @ExceptionHandler(ObjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserException(ObjectNotFoundException e) {
        return new ErrorResponse().message(e.getMessage());
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleServiceException(ServiceUnavailableException e) {
        return new ErrorResponse().message(e.getMessage());
    }
}
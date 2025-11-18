package com.spring5.movieservice.domain.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException() {}

    public BadRequestException(String msg) {
        super(msg);
    }

    public BadRequestException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public BadRequestException(Throwable cause) {
        super(cause);
    }
}

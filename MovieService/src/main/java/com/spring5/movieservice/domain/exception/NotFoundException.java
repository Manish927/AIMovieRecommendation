package com.spring5.movieservice.domain.exception;


public class NotFoundException extends RuntimeException {
    public NotFoundException() {}

    public NotFoundException(String msg) {
        super(msg);
    }

    public NotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public NotFoundException(Throwable cause) {
        super(cause);
    }
}

package com.spring5.movieservice.domain.exception;

public class EventProcessingException extends RuntimeException{
    public EventProcessingException() {}

    public EventProcessingException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public EventProcessingException(String msg) {
        super(msg);
    }

    public EventProcessingException(Throwable cause) {
        super(cause);
    }
}

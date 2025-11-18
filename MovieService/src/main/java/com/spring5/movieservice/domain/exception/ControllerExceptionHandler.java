package com.spring5.movieservice.domain.exception;

import com.spring5.movieservice.common.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class ControllerExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(BadRequestException.class)

    public @ResponseBody HttpErrorInfo handleBadRequestExceptions(ServerHttpRequest request, BadRequestException ex) {
        return createHttpErrorInfo(BAD_REQUEST, request, ex);
    }

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public @ResponseBody HttpErrorInfo handleNotFoundException(ServerHttpRequest request, NotFoundException ex) {
        return createHttpErrorInfo(NOT_FOUND, request, ex);
    }

    @ResponseStatus(UNPROCESSABLE_ENTITY)
    @ExceptionHandler(InvalidInputException.class)
    public @ResponseBody HttpErrorInfo handleInvalidInputException(ServerHttpRequest request, InvalidInputException ex) {
        return createHttpErrorInfo(UNPROCESSABLE_ENTITY, request, ex);
    }
    private HttpErrorInfo createHttpErrorInfo(HttpStatus httpStatus, ServerHttpRequest request, Exception ex) {
        final String path = request.getPath().pathWithinApplication().value();
        final String msg = ex.getMessage();
        LOG.debug("Returning HTTP status: {} for path: {}, message: {}", httpStatus, path, msg);
        return new HttpErrorInfo(httpStatus, path, msg);
    }
}

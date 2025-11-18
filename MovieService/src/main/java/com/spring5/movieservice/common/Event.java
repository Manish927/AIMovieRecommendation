package com.spring5.movieservice.common;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.key.ZonedDateTimeKeySerializer;

import java.time.ZonedDateTime;

public class Event<K, T> {

    public enum Type {
        CREATE,
        DELETE
    }

    private final Type eventType;
    private final K key;
    private final T data;
    private final ZonedDateTime eventCreatedAt;

    public Event() {
        this.eventType = null;
        this.key = null;
        this.data = null;
        this.eventCreatedAt = ZonedDateTime.now();
    }

    public Event(Type eventType, K key, T data) {
        this.eventType  = eventType;
        this.key = key;
        this.data = data;
        this.eventCreatedAt = ZonedDateTime.now();
    }
    public Type getEventType() {
        return eventType;
    }

    public K getKey() {
        return key;
    }

    public T getData() {
        return data;
    }

    @JsonSerialize(using = ZonedDateTimeKeySerializer.class)
    public ZonedDateTime getEventCreatedAt() {
        return eventCreatedAt;
    }
}

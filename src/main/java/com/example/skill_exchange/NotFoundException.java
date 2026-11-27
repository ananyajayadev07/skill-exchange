package com.example.skill_exchange;

/** Maps to HTTP 404. Extends IllegalArgumentException so the console profile keeps working. */
public class NotFoundException extends IllegalArgumentException {
    public NotFoundException(String message) {
        super(message);
    }
}

package com.example.skill_exchange;

/** Maps to HTTP 403 (logged in, but not allowed to touch this resource). */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}

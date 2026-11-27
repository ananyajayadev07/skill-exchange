package com.example.skill_exchange;

/** Maps to HTTP 401 (bad credentials). */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}

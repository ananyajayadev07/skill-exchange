package com.example.skill_exchange;

import org.springframework.security.core.Authentication;

/** Helpers for reading who is making the current request. The principal is the student's id. */
final class Auth {

    private Auth() {
    }

    static Long id(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }

    static void requireSelf(Authentication authentication, Long studentId) {
        if (!id(authentication).equals(studentId)) {
            throw new ForbiddenException("You can only change your own data.");
        }
    }
}

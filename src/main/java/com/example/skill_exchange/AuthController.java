package com.example.skill_exchange;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final StudentService students;
    private final JwtService jwt;

    public AuthController(StudentService students, JwtService jwt) {
        this.students = students;
        this.jwt = jwt;
    }

    record RegisterRequest(@NotBlank @Size(max = 80) String name,
                           @NotBlank @Email String email,
                           @Size(max = 300) String bio,
                           @NotBlank @Size(min = 8, max = 72) String password) {}

    record LoginRequest(@NotBlank String email, @NotBlank String password) {}

    record AuthResponse(String token, MeView user) {}

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest r) {
        Student s = students.registerStudent(r.name(), r.email(), r.bio(), r.password());
        return new AuthResponse(jwt.issue(s), MeView.from(s));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest r) {
        Student s = students.authenticate(r.email(), r.password());
        return new AuthResponse(jwt.issue(s), MeView.from(s));
    }

    @GetMapping("/me")
    public MeView me(Authentication authentication) {
        return MeView.from(students.getStudent(Auth.id(authentication)));
    }
}

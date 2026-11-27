package com.example.skill_exchange;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    record UpdateRequest(@NotBlank String name, @Size(max = 300) String bio) {}

    record SkillRequest(@NotBlank @Size(max = 40) String skill, boolean teaching) {}

    /** Optional ?skill=java filters to students who teach or want to learn that skill. */
    @GetMapping
    public List<Student> all(@RequestParam(required = false) String skill) {
        return service.search(skill);
    }

    @GetMapping("/{id}")
    public Student one(@PathVariable Long id) {
        return service.getStudent(id);
    }

    @PutMapping("/{id}")
    public Student update(@PathVariable Long id, @Valid @RequestBody UpdateRequest r, Authentication auth) {
        Auth.requireSelf(auth, id);
        return service.updateStudent(id, r.name(), r.bio());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        Auth.requireSelf(auth, id);
        service.deleteStudent(id);
    }

    @PostMapping("/{id}/skills")
    public Student addSkill(@PathVariable Long id, @Valid @RequestBody SkillRequest r, Authentication auth) {
        Auth.requireSelf(auth, id);
        return service.addSkill(id, r.skill(), r.teaching());
    }

    @DeleteMapping("/{id}/skills")
    public Student removeSkill(@PathVariable Long id,
                               @RequestParam String skill,
                               @RequestParam boolean teaching,
                               Authentication auth) {
        Auth.requireSelf(auth, id);
        return service.removeSkill(id, skill, teaching);
    }

    @GetMapping("/{id}/matches")
    public List<MatchResult> matches(@PathVariable Long id, Authentication auth) {
        Auth.requireSelf(auth, id);
        return service.findMatchesWithScore(id);
    }
}

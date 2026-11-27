package com.example.skill_exchange;

import java.util.Set;

/** The logged-in student's own profile. Unlike Student, it includes the email. */
public record MeView(Long id, String name, String email, String bio,
                     Set<String> skillsToTeach, Set<String> skillsToLearn) {

    static MeView from(Student s) {
        return new MeView(s.getId(), s.getName(), s.getEmail(), s.getBio(),
                Set.copyOf(s.getSkillsToTeach()), Set.copyOf(s.getSkillsToLearn()));
    }
}

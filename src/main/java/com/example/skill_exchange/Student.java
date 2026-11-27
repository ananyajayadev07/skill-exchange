package com.example.skill_exchange;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // Not exposed in JSON: other users shouldn't see each other's emails (see MeView for your own).
    @JsonIgnore
    @Column(unique = true, nullable = false)
    private String email;

    @JsonIgnore
    private String passwordHash;

    @Column(length = 300)
    private String bio;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> skillsToTeach = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> skillsToLearn = new HashSet<>();

    public Student() {
    }

    public Student(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public Set<String> getSkillsToTeach() { return skillsToTeach; }
    public Set<String> getSkillsToLearn() { return skillsToLearn; }
}

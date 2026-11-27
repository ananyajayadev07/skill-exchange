package com.example.skill_exchange;

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

    @Column(unique = true, nullable = false)
    private String email;

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

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public Set<String> getSkillsToTeach() { return skillsToTeach; }
    public Set<String> getSkillsToLearn() { return skillsToLearn; }
}

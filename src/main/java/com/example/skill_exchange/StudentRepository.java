package com.example.skill_exchange;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("select distinct s from Student s "
            + "where :skill member of s.skillsToTeach or :skill member of s.skillsToLearn")
    List<Student> findBySkill(@Param("skill") String skill);
}

package com.example.skill_exchange;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class StudentService {

    private final StudentRepository repository;
    private final SwapRepository swapRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentRepository repository, SwapRepository swapRepository,
                          PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.swapRepository = swapRepository;
        this.passwordEncoder = passwordEncoder;
    }

    static String normalizeSkill(String skill) {
        if (skill == null || skill.isBlank()) {
            throw new IllegalArgumentException("Skill cannot be empty.");
        }
        String s = skill.trim().toLowerCase(Locale.ROOT);
        if (s.length() > 40) {
            throw new IllegalArgumentException("Skill must be 40 characters or fewer.");
        }
        return s;
    }

    @Transactional
    public Student registerStudent(String name, String email) {
        return registerStudent(name, email, null);
    }

    @Transactional
    public Student registerStudent(String name, String email, String bio) {
        return registerStudent(name, email, bio, null);
    }

    /** Registers a student. A null password creates a profile that can't log in (console mode). */
    @Transactional
    public Student registerStudent(String name, String email, String bio, String password) {
        if (password != null && (password.length() < 8 || password.length() > 72)) {
            throw new IllegalArgumentException("Password must be 8 to 72 characters.");
        }
        if (name == null || name.isBlank() || email == null || email.isBlank()) {
            throw new IllegalArgumentException("Name and email cannot be empty.");
        }
        email = email.trim();
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("This email is already registered.");
        }
        Student student = new Student(name.trim(), email);
        student.setBio(bio == null || bio.isBlank() ? null : bio.trim());
        if (password != null) {
            student.setPasswordHash(passwordEncoder.encode(password));
        }
        return repository.save(student);
    }

    @Transactional(readOnly = true)
    public Student authenticate(String email, String password) {
        Student student = repository.findByEmailIgnoreCase(email == null ? "" : email.trim()).orElse(null);
        if (student == null || student.getPasswordHash() == null || password == null
                || !passwordEncoder.matches(password, student.getPasswordHash())) {
            throw new UnauthorizedException("Incorrect email or password.");
        }
        return student;
    }

    @Transactional(readOnly = true)
    public Student getStudent(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Student ID not found."));
    }

    @Transactional
    public Student updateStudent(Long id, String name, String bio) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        Student student = getStudent(id);
        student.setName(name.trim());
        student.setBio(bio == null || bio.isBlank() ? null : bio.trim());
        return repository.save(student);
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student student = getStudent(id);
        swapRepository.deleteByRequesterIdOrReceiverId(id, id);
        repository.delete(student);
    }

    @Transactional
    public Student addSkill(Long studentId, String skill, boolean teaching) {
        String normalized = normalizeSkill(skill);
        Student student = getStudent(studentId);
        (teaching ? student.getSkillsToTeach() : student.getSkillsToLearn()).add(normalized);
        return repository.save(student);
    }

    @Transactional
    public Student removeSkill(Long studentId, String skill, boolean teaching) {
        String normalized = normalizeSkill(skill);
        Student student = getStudent(studentId);
        (teaching ? student.getSkillsToTeach() : student.getSkillsToLearn()).remove(normalized);
        return repository.save(student);
    }

    @Transactional(readOnly = true)
    public List<Student> getAllStudents() {
        return repository.findAll();
    }

    /** All students, or only those who teach/learn the given skill. */
    @Transactional(readOnly = true)
    public List<Student> search(String skill) {
        if (skill == null || skill.isBlank()) {
            return repository.findAll();
        }
        return repository.findBySkill(skill.trim().toLowerCase(Locale.ROOT));
    }

    /** Plain list of mutual matches (used by the console profile). */
    @Transactional(readOnly = true)
    public List<Student> findMatches(Long studentId) {
        return findMatchesWithScore(studentId).stream().map(MatchResult::student).toList();
    }

    /** Mutual matches ranked by how many skills line up, best first. */
    @Transactional(readOnly = true)
    public List<MatchResult> findMatchesWithScore(Long studentId) {
        Student me = getStudent(studentId);
        List<MatchResult> results = new ArrayList<>();

        for (Student other : repository.findAll()) {
            if (other.getId().equals(studentId)) continue;

            Set<String> theyTeachYou = new TreeSet<>(other.getSkillsToTeach());
            theyTeachYou.retainAll(me.getSkillsToLearn());

            Set<String> youTeachThem = new TreeSet<>(me.getSkillsToTeach());
            youTeachThem.retainAll(other.getSkillsToLearn());

            if (!theyTeachYou.isEmpty() && !youTeachThem.isEmpty()) {
                results.add(new MatchResult(other,
                        theyTeachYou.size() + youTeachThem.size(), theyTeachYou, youTeachThem));
            }
        }
        results.sort(Comparator.comparingInt(MatchResult::score).reversed()
                .thenComparing(m -> m.student().getName(), String.CASE_INSENSITIVE_ORDER));
        return results;
    }
}

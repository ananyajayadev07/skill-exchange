package com.example.skill_exchange;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class StudentServiceTests {

    @Autowired StudentService students;
    @Autowired SwapService swaps;
    @Autowired JwtService jwt;

    private Student student(String name, String email) {
        return students.registerStudent(name, email);
    }

    @Test
    void rejectsDuplicateEmail() {
        student("A", "dup@example.com");
        assertThrows(IllegalArgumentException.class, () -> student("B", "DUP@example.com"));
    }

    @Test
    void skillsAreNormalisedAndRemovable() {
        Student s = student("A", "a@example.com");
        students.addSkill(s.getId(), "  Java ", true);
        assertTrue(students.getStudent(s.getId()).getSkillsToTeach().contains("java"));
        students.removeSkill(s.getId(), "JAVA", true);
        assertTrue(students.getStudent(s.getId()).getSkillsToTeach().isEmpty());
    }

    @Test
    void matchesNeedToBeMutualAndAreScored() {
        Student a = student("A", "a2@example.com");
        Student b = student("B", "b2@example.com");
        Student c = student("C", "c2@example.com");

        students.addSkill(a.getId(), "java", true);
        students.addSkill(a.getId(), "figma", false);
        students.addSkill(b.getId(), "figma", true);
        students.addSkill(b.getId(), "java", false);
        students.addSkill(c.getId(), "figma", true);   // teaches what A wants, but wants nothing from A

        List<MatchResult> matches = students.findMatchesWithScore(a.getId());
        assertEquals(1, matches.size());
        assertEquals(b.getId(), matches.get(0).student().getId());
        assertEquals(2, matches.get(0).score());
    }

    @Test
    void swapLifecycle() {
        Student a = student("A", "a3@example.com");
        Student b = student("B", "b3@example.com");
        students.addSkill(a.getId(), "java", true);
        students.addSkill(b.getId(), "figma", true);

        SwapService.SwapView swap = swaps.create(a.getId(), b.getId(), "java", "figma");
        assertEquals(SwapStatus.PENDING, swap.status());
        assertThrows(IllegalArgumentException.class,
                () -> swaps.create(a.getId(), b.getId(), "java", "figma"));   // duplicate
        assertThrows(IllegalArgumentException.class,
                () -> swaps.updateStatus(swap.id(), b.getId(), SwapStatus.COMPLETED));   // must accept first
        assertThrows(ForbiddenException.class,
                () -> swaps.updateStatus(swap.id(), a.getId(), SwapStatus.ACCEPTED));    // requester can't accept own request

        assertEquals(SwapStatus.ACCEPTED, swaps.updateStatus(swap.id(), b.getId(), SwapStatus.ACCEPTED).status());
        assertEquals(SwapStatus.COMPLETED, swaps.updateStatus(swap.id(), a.getId(), SwapStatus.COMPLETED).status());
    }

    @Test
    void strangersCannotTouchASwap() {
        Student a = student("A", "a5@example.com");
        Student b = student("B", "b5@example.com");
        Student c = student("C", "c5@example.com");
        students.addSkill(a.getId(), "java", true);
        students.addSkill(b.getId(), "sql", true);
        SwapService.SwapView swap = swaps.create(a.getId(), b.getId(), "java", "sql");
        assertThrows(ForbiddenException.class,
                () -> swaps.updateStatus(swap.id(), c.getId(), SwapStatus.ACCEPTED));
    }

    @Test
    void loginChecksPasswordAndTokensRoundTrip() {
        Student s = students.registerStudent("A", "auth@example.com", null, "password123");
        assertNotEquals("password123", s.getPasswordHash());   // stored hashed, never in plain text

        assertEquals(s.getId(), students.authenticate("AUTH@example.com", "password123").getId());
        assertThrows(UnauthorizedException.class, () -> students.authenticate("auth@example.com", "wrong-pass"));
        assertThrows(UnauthorizedException.class, () -> students.authenticate("nobody@example.com", "password123"));

        String token = jwt.issue(s);
        assertEquals(s.getId(), jwt.parse(token));
        assertThrows(JwtException.class, () -> jwt.parse(token + "x"));
    }

    @Test
    void rejectsShortPasswords() {
        assertThrows(IllegalArgumentException.class,
                () -> students.registerStudent("A", "short@example.com", null, "1234567"));
    }

    @Test
    void consoleProfilesWithoutPasswordCannotLogIn() {
        student("Console", "console@example.com");
        assertThrows(UnauthorizedException.class, () -> students.authenticate("console@example.com", "anything1"));
    }

    @Test
    void deletingStudentRemovesTheirSwaps() {
        Student a = student("A", "a4@example.com");
        Student b = student("B", "b4@example.com");
        students.addSkill(a.getId(), "java", true);
        students.addSkill(b.getId(), "sql", true);
        swaps.create(a.getId(), b.getId(), "java", "sql");

        students.deleteStudent(a.getId());
        assertTrue(swaps.forStudent(b.getId()).isEmpty());
        assertThrows(NotFoundException.class, () -> students.getStudent(a.getId()));
    }
}

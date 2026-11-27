package com.example.skill_exchange;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Seeds a few students when running with the h2 profile, so the UI has something to show. */
@Configuration
@Profile("h2")
public class DemoData {

    @Bean
    CommandLineRunner seed(StudentService s) {
        return args -> {
            if (!s.getAllStudents().isEmpty()) return;
            Student a = s.registerStudent("Asha Rao", "asha@example.com", "CSE, 3rd year. Happy to trade code for design tips.");
            Student b = s.registerStudent("Ben Carter", "ben@example.com", "Design student who wants to get into backend.");
            Student c = s.registerStudent("Chitra Iyer", "chitra@example.com", "Guitar on weekends, Python on weekdays.");
            Student d = s.registerStudent("Dev Malhotra", "dev@example.com", "Preparing for placements.");

            for (String k : new String[]{"java", "spring boot", "sql"}) s.addSkill(a.getId(), k, true);
            for (String k : new String[]{"figma", "guitar"}) s.addSkill(a.getId(), k, false);

            for (String k : new String[]{"figma", "photography"}) s.addSkill(b.getId(), k, true);
            for (String k : new String[]{"java", "sql"}) s.addSkill(b.getId(), k, false);

            for (String k : new String[]{"guitar", "python"}) s.addSkill(c.getId(), k, true);
            for (String k : new String[]{"spring boot", "figma"}) s.addSkill(c.getId(), k, false);

            for (String k : new String[]{"dsa", "python"}) s.addSkill(d.getId(), k, true);
            s.addSkill(d.getId(), "photography", false);
        };
    }
}

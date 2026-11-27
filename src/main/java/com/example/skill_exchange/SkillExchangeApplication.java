package com.example.skill_exchange;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.Scanner;

@SpringBootApplication
public class SkillExchangeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkillExchangeApplication.class, args);
    }

    @Bean
    @Profile("console")
    CommandLineRunner run(StudentService service) {
        return args -> {
            try (Scanner scanner = new Scanner(System.in)) {
                boolean running = true;

                while (running) {
                    System.out.println("\n===== STUDENT SKILL EXCHANGE PLATFORM =====");
                    System.out.println("1. Register Student");
                    System.out.println("2. Add Skill");
                    System.out.println("3. View All Students");
                    System.out.println("4. Find Skill Matches");
                    System.out.println("5. Exit");
                    System.out.print("Enter your choice: ");

                    String choice = scanner.nextLine().trim();

                    try {
                        switch (choice) {
                            case "1" -> {
                                System.out.print("Enter name: ");
                                String name = scanner.nextLine();

                                System.out.print("Enter email: ");
                                String email = scanner.nextLine();

                                Student student =
                                        service.registerStudent(name, email);

                                System.out.println(
                                        "Student registered successfully!");
                                System.out.println("Student ID: "
                                        + student.getId());
                            }

                            case "2" -> {
                                System.out.print("Enter student ID: ");
                                Long id = Long.parseLong(
                                        scanner.nextLine().trim());

                                System.out.print(
                                        "Enter skill name: ");
                                String skill = scanner.nextLine();

                                System.out.println("1. Skill to teach");
                                System.out.println("2. Skill to learn");
                                System.out.print("Choose type: ");

                                String type = scanner.nextLine().trim();

                                if (!type.equals("1")
                                        && !type.equals("2")) {
                                    System.out.println(
                                            "Invalid skill type.");
                                    break;
                                }

                                service.addSkill(
                                        id, skill, type.equals("1"));

                                System.out.println(
                                        "Skill added successfully!");
                            }

                            case "3" -> {
                                List<Student> students =
                                        service.getAllStudents();

                                if (students.isEmpty()) {
                                    System.out.println(
                                            "No students registered yet.");
                                } else {
                                    for (Student student : students) {
                                        System.out.println(
                                                "\nStudent ID: "
                                                        + student.getId());
                                        System.out.println(
                                                "Name: "
                                                        + student.getName());
                                        System.out.println(
                                                "Email: "
                                                        + student.getEmail());
                                        System.out.println(
                                                "Skills to teach: "
                                                        + student.getSkillsToTeach());
                                        System.out.println(
                                                "Skills to learn: "
                                                        + student.getSkillsToLearn());
                                    }
                                }
                            }

                            case "4" -> {
                                System.out.print(
                                        "Enter your student ID: ");

                                Long id = Long.parseLong(
                                        scanner.nextLine().trim());

                                List<Student> matches =
                                        service.findMatches(id);

                                if (matches.isEmpty()) {
                                    System.out.println(
                                            "No mutual skill matches found.");
                                } else {
                                    System.out.println(
                                            "Mutual skill matches:");

                                    for (Student match : matches) {
                                        System.out.println(
                                                match.getId() + " - "
                                                        + match.getName()
                                                        + " (" + match.getEmail()
                                                        + ")");
                                        System.out.println(
                                                "Can teach: "
                                                        + match.getSkillsToTeach());
                                        System.out.println(
                                                "Wants to learn: "
                                                        + match.getSkillsToLearn());
                                    }
                                }
                            }

                            case "5" -> {
                                running = false;
                                System.out.println(
                                        "Thank you for using the platform!");
                            }

                            default -> System.out.println(
                                    "Invalid choice. Please try again.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println(
                                "Please enter a valid numeric ID.");
                    } catch (IllegalArgumentException e) {
                        System.out.println("Error: " + e.getMessage());
                    } catch (Exception e) {
                        System.out.println(
                                "An error occurred: " + e.getMessage());
                    }
                }
            }
        };
    }
}

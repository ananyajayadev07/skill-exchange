# SkillSwap – Student Skill Exchange Platform

Students list what they can **teach** and what they want to **learn**. SkillSwap finds mutual matches,
ranks them, and lets students send, accept and complete swap requests.

**Stack:** Java 21, Spring Boot, Spring Data JPA / Hibernate, MySQL (H2 for quick demos), Bean Validation, JUnit, Docker, GitHub Actions. The web UI is plain HTML/CSS/JS served by Spring Boot.

## Features
- Student profiles with bio, skills to teach and skills to learn (edit, delete)
- Mutual matching with a score, best matches first
- Search students by skill
- Swap requests with a lifecycle: pending → accepted / declined → completed
- Input validation and consistent JSON errors (400 / 404 / 409)
- Web UI at `/`, REST API under `/api`
- Console mode kept from the original version (`--spring.profiles.active=console`)

## Run it

**Fastest (no database needed, with demo data):**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```
Open http://localhost:8080

**With MySQL:**
```bash
export DB_PASSWORD=your-mysql-password      # DB_URL and DB_USERNAME are optional overrides
./mvnw spring-boot:run
```

**With Docker (app + MySQL):**
```bash
cp .env.example .env        # set a password
docker compose up --build
```

**Tests:** `./mvnw test` (uses H2, no setup).

## API

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/students` | Register `{name, email, bio?}` |
| GET | `/api/students?skill=java` | List students, optionally filtered by skill |
| GET / PUT / DELETE | `/api/students/{id}` | Read, update `{name, bio}`, delete |
| POST | `/api/students/{id}/skills` | Add skill `{skill, teaching}` |
| DELETE | `/api/students/{id}/skills?skill=x&teaching=true` | Remove a skill |
| GET | `/api/students/{id}/matches` | Ranked mutual matches |
| POST | `/api/swaps` | Create `{requesterId, receiverId, skillOffered, skillRequested}` |
| GET | `/api/students/{id}/swaps` | Sent and received requests |
| PATCH | `/api/swaps/{id}` | Update status `{status: ACCEPTED \| REJECTED \| COMPLETED}` |

## Roadmap
- Spring Security + JWT login (the UI currently lets you pick which student you're acting as)
- OpenAPI docs, ratings after completed swaps, notifications

# SkillSwap – Student Skill Exchange Platform

Students list what they can **teach** and what they want to **learn**. SkillSwap finds mutual matches,
ranks them, and lets students send, accept and complete swap requests.

**Stack:** Java 21, Spring Boot, Spring Security + JWT, Spring Data JPA / Hibernate, MySQL (H2 for quick demos), Bean Validation, JUnit, Docker, GitHub Actions. The web UI is plain HTML/CSS/JS served by Spring Boot.

## Features
- Sign up and log in with JWT authentication; passwords are stored as BCrypt hashes
- Ownership rules: you can only edit your own profile, and only the receiver can accept a swap request
- Other students' emails are never exposed through the API
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
Open http://localhost:8080 and log in with a demo account, for example `asha@example.com` / `password123` (all demo accounts use that password; they exist only in the `h2` profile).

**With MySQL:**
```bash
export DB_PASSWORD=your-mysql-password      # DB_URL and DB_USERNAME are optional overrides
export JWT_SECRET=a-long-random-string-of-32+-characters   # optional; random per start if unset
./mvnw spring-boot:run
```

**With Docker (app + MySQL):**
```bash
cp .env.example .env        # set a password
docker compose up --build
```

**Tests:** `./mvnw test` (uses H2, no setup).

## API

Everything except register and login needs the header `Authorization: Bearer <token>`.

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Create an account `{name, email, bio?, password}`, returns `{token, user}` |
| POST | `/api/auth/login` | Log in `{email, password}`, returns `{token, user}` |
| GET | `/api/auth/me` | Your own profile |
| GET | `/api/students?skill=java` | List students, optionally filtered by skill |
| GET | `/api/students/{id}` | One student's public profile |
| PUT / DELETE | `/api/students/{id}` | Update `{name, bio}` or delete (own account only) |
| POST | `/api/students/{id}/skills` | Add skill `{skill, teaching}` (own account only) |
| DELETE | `/api/students/{id}/skills?skill=x&teaching=true` | Remove a skill (own account only) |
| GET | `/api/students/{id}/matches` | Your ranked mutual matches |
| POST | `/api/swaps` | Request a swap `{receiverId, skillOffered, skillRequested}` |
| GET | `/api/students/{id}/swaps` | Your sent and received requests |
| PATCH | `/api/swaps/{id}` | `{status}`: receiver sets ACCEPTED or REJECTED; either person sets COMPLETED |

Errors come back as `{"error": "..."}` with 400, 401, 403, 404 or 409.

## Roadmap
- Refresh tokens and an HttpOnly-cookie option
- OpenAPI docs, ratings after completed swaps, notifications

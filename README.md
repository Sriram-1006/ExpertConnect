# ExpertConnect

A Spring Boot REST API where people ask questions and **verified experts** answer
them. Experts are not self-declared: a user applies, an ADMIN reviews the
application, and only a verified expert can post answers. Authentication uses
JWT, passwords are hashed with BCrypt.

The frontend (React) and the AI answer-assistant are **planned**, not built.

---

## Status at a glance

| Area | Status |
|------|--------|
| Part 1 — Users | ✅ IMPLEMENTED |
| Part 2 — Questions + ownership | ✅ IMPLEMENTED |
| Part 3 — Expert applications + Answers | ✅ IMPLEMENTED |
| Part 4 — Spring Security + JWT | ✅ IMPLEMENTED |
| React frontend | ⏳ PLANNED |
| AI answer assistant | ⏳ PLANNED |

---

## Features

- User registration and login (JWT), BCrypt password hashing.
- Public read-only question browsing.
- Authenticated users create/update/delete **their own** questions.
- Expert application workflow with ADMIN approval/rejection.
- Verified-expert-only answer creation, with answer editing/deleting limited to
  the author.
- Question owner can accept exactly one answer.
- Role-based authorization (USER / EXPERT / ADMIN) and authenticated ownership.
- Central JSON error handling (400 / 401 / 403 / 404 / 409) with no stack traces.

---

## Technology stack

- Java 21 (built and run on JDK 26)
- Spring Boot 4.1.1 (Spring Framework 7)
- Spring Security 7.1.1
- Spring Data JPA / Hibernate 7
- MySQL 8
- JJWT 0.12.6 (JWT)
- Maven

---

## Architecture

Layered, one direction of dependency:

```
Controller  ->  Service  ->  Repository  ->  MySQL
     |              |
   DTOs        Entities (JPA)
     |
Security (JWT filter, SecurityConfig) wraps every request
```

- **DTOs** are used for every request and response; JPA entities are never
  serialised directly, so there is no recursive/circular JSON and no field
  leakage (e.g. `password`).
- **Authorization lives in the service layer** (ownership checks) and in
  `SecurityConfig` (role rules). Identity always comes from the JWT principal
  (`AuthUser`), never from a client-supplied id.

---

## Project structure

```
src/main/java/ExpertConnect/
├── ExpertconnectApplication.java
├── config/         AdminInitializer (bootstrap ADMIN)
├── controller/     Auth, User, Question, Expert, Answer, Hello
├── dto/            Request/response records
├── entity/         User, Question, ExpertApplication, Answer, enums
├── exception/      GlobalExceptionHandler + custom exceptions
├── repository/     Spring Data JPA repositories
├── security/       SecurityConfig, JwtService, JwtAuthFilter, AuthUser
└── service/        UserService, AuthService, QuestionService,
                    ExpertService, AnswerService
src/main/resources/application.properties
src/test/java/ExpertConnect/ExpertconnectApplicationTests.java
```

---

## Database relationships

```
User 1 ──── * Question
User 1 ──── * ExpertApplication
User 1 ──── * Answer
Question 1 ──── * Answer
```

- `ExpertApplication.verificationStatus`: `PENDING` | `VERIFIED` | `REJECTED`.
- `Answer.accepted`: at most one accepted answer per question (enforced in
  `AnswerService`).
- Tables are auto-created/updated by Hibernate (`spring.jpa.hibernate.ddl-auto=update`).

---

## Workflows

### User
Register (`/api/auth/register`) → receive a JWT on login (`/api/auth/login`) →
create questions, manage their own questions, apply to become an expert.

### Question
Any visitor may browse. An authenticated user creates a question; only the asker
can update or delete it (`403` otherwise).

### Expert verification
1. An authenticated user submits an application (`POST /api/experts/apply`) →
   `PENDING`.
2. An ADMIN lists applications (`GET /api/experts`, `GET /api/experts/status/PENDING`).
3. On approve → application `VERIFIED` **and** the user's role becomes `EXPERT`.
4. On reject → application `REJECTED`; the user stays a normal USER.

A pending or rejected applicant is **not** allowed to answer.

### Answer
A `VERIFIED` expert posts an answer. The backend re-checks, on every write, that
the caller is an `EXPERT` **and** has a `VERIFIED` application — the JWT role
alone is not trusted. Authors edit/delete their own answers. The question owner
may accept exactly one answer; accepting a new one unaccepts the previous.

---

## Authentication

- `POST /api/auth/register` — creates a normal `USER` (no role input).
- `POST /api/auth/login` — returns a JWT for valid credentials (`401` otherwise).
- Protected endpoints require `Authorization: Bearer <token>`.
- Missing, invalid or expired tokens → `401` JSON (no stack trace).

### JWT
- Signed HS256 with the key from `EXPERTCONNECT_JWT_SECRET`.
- Claims: subject = user id, plus `email` and `role`.
- Default lifetime 24h (`EXPERTCONNECT_JWT_EXPIRATION_MS`).

The filter reloads the user from the database on each request, so a role change
(e.g. after expert approval) takes effect immediately without logging in again.

---

## Roles and authorization

| Role | Permissions |
|------|-------------|
| `USER` | Create/edit/delete **own** questions; apply as expert; browse questions |
| `EXPERT` | Everything a USER can do, plus create/edit/delete **own** answers (only if application is `VERIFIED`) |
| `ADMIN` | View/approve/reject expert applications; view the user directory |

Notes:
- Public registration always creates `USER`. ADMIN accounts are bootstrapped from
  configuration, never through public registration.
- `SecurityConfig` enforces roles; services additionally enforce ownership.

---

## API endpoints

| Method | Path | Access |
|--------|------|--------|
| POST | `/api/auth/register` | public |
| POST | `/api/auth/login` | public |
| POST | `/api/users/register` | public (legacy, always USER) |
| GET | `/api/users/me` | authenticated |
| GET | `/api/users` | ADMIN |
| GET | `/api/users/{id}` | ADMIN |
| GET | `/api/users/email/{email}` | ADMIN |
| POST | `/api/questions` | authenticated |
| GET | `/api/questions` | public |
| GET | `/api/questions/{id}` | public |
| GET | `/api/questions/category/{category}` | public |
| GET | `/api/questions/user/{userId}` | public |
| PUT | `/api/questions/{id}` | owner |
| DELETE | `/api/questions/{id}` | owner |
| POST | `/api/experts/apply` | authenticated |
| GET | `/api/experts` | ADMIN |
| GET | `/api/experts/{id}` | ADMIN |
| GET | `/api/experts/status/{status}` | ADMIN |
| PUT | `/api/experts/{id}/approve` | ADMIN |
| PUT | `/api/experts/{id}/reject` | ADMIN |
| POST | `/api/questions/{questionId}/answers` | verified EXPERT |
| GET | `/api/questions/{questionId}/answers` | public |
| PUT | `/api/answers/{answerId}` | author |
| DELETE | `/api/answers/{answerId}` | author |
| PUT | `/api/questions/{questionId}/answers/{answerId}/accept` | question owner |
| GET | `/hello`, `/greeting` | public |

Valid `Category` values: `TECHNOLOGY, PROGRAMMING, AI_ML, DATABASE, CAREER,
FINANCE, LEGAL, HEALTH, EDUCATION, OTHER`.

### Example

```bash
# register then login
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" \
  -d '{"name":"Ada","email":"ada@example.com","password":"Password123"}'
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" \
  -d '{"email":"ada@example.com","password":"Password123"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# create a question
curl -X POST http://localhost:8080/api/questions -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"How do I learn Spring?","description":"A clear roadmap please.","category":"PROGRAMMING"}'
```

---

## Environment variables

| Variable | Purpose | Default |
|----------|---------|---------|
| `EXPERTCONNECT_DB_URL` | JDBC URL | `jdbc:mysql://localhost:3306/expertconnect?...` |
| `EXPERTCONNECT_DB_USERNAME` | DB user | `root` |
| `EXPERTCONNECT_DB_PASSWORD` | DB password | *(empty)* |
| `EXPERTCONNECT_JWT_SECRET` | JWT signing key (≥32 bytes) | dev-only fallback |
| `EXPERTCONNECT_JWT_EXPIRATION_MS` | token lifetime | `86400000` |
| `EXPERTCONNECT_ADMIN_EMAIL` | bootstrap admin email | `admin@expertconnect.local` |
| `EXPERTCONNECT_ADMIN_PASSWORD` | bootstrap admin password | `admin12345` |

See `.env.example` for a copy-paste template. **Never commit real values.**

> ⚠️ The JWT secret fallback and the default admin password are for local
> development only. Always override them.

---

## Setup

Prerequisites: JDK 21+, Maven, MySQL 8.

1. Create the database once:
   ```sql
   CREATE DATABASE expertconnect;
   ```
2. Set the environment variables (never written to source), for example in bash:
   ```bash
   export EXPERTCONNECT_DB_PASSWORD=your_mysql_password
   export EXPERTCONNECT_JWT_SECRET=your_long_random_secret
   export EXPERTCONNECT_ADMIN_EMAIL=admin@example.com
   export EXPERTCONNECT_ADMIN_PASSWORD=your_admin_password
   ```
3. Run:
   ```bash
   cd D:/Code/ExpertConnect/expertconnect
   ./mvnw spring-boot:run        # or: mvn spring-boot:run
   ```
   The API starts on `http://localhost:8080`. Tables are created automatically
   and the ADMIN account is bootstrapped if it does not exist.

---

## Testing

Build and run the test lifecycle (from the Maven project root):

```bash
cd D:/Code/ExpertConnect/expertconnect
EXPERTCONNECT_DB_PASSWORD=your_mysql_password mvn clean package
```

During development the full API was exercised with HTTP (`curl`) requests
against a running instance, covering the whole chain
register → login → JWT → create question → apply as expert → admin approve →
answer → accept, plus validation and `401/403/404/409` handling, ownership and
persistence across a restart.

Results: Part 3 workflow **33/33**, Part 4 + regression **41/41**, and rows were
confirmed to persist after stopping and restarting the application.

> The repository currently contains only the Maven context-load test. Automated
> `MockMvc`/integration tests for the endpoints are **PLANNED**.

---

## Known limitations

- **Legacy plain-text users**: accounts created before Part 4 stored passwords in
  plain text and therefore cannot log in; they must be re-registered. All new
  accounts use BCrypt.
- The JWT secret and bootstrap-admin credentials have development defaults; they
  must be overridden in real deployments.
- The bootstrap ADMIN password is configured via environment variables (there is
  no self-service admin creation by design).
- Answer/Expert listing endpoints for admins are not paginated.
- `spring.jpa.show-sql=true` is on (fine for a college project, noisy for prod).

---

## Future (PLANNED — not implemented)

- **React frontend**: a separate app will consume this API. CORS is already
  configured for `http://localhost:3000` and `http://localhost:5173`.
- **AI answer assistant**: no AI code exists yet.

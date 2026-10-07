# ExpertConnect — API Audit (Part 1 + Part 2)

Audit date: 2026-10-07
Build: `expertconnect` 0.0.1-SNAPSHOT (Spring Boot 4.1.1, Java 21, MySQL 8)
Target: `http://localhost:8080`
Method: live HTTP requests against the running application (`curl`), plus a static
read of the controllers, services, DTOs, entities and exception handler.

Scope:

- **Part 1** — Users: registration, listing, lookup (`/api/users`).
- **Part 2** — Questions: create, read, update, delete and filters
  (`/api/questions`), including ownership enforcement.

A quick smoke check of `/hello` and `/greeting` was also run.

## 1. Part 1 — User API results

| # | Request | Expected | Actual | Status |
|---|---------|----------|--------|--------|
| 1 | `POST /api/users/register` valid body | 201 + user | 201, `id` returned, password omitted from response | PASS |
| 2 | Register duplicate email (different case) | 409 | 409 `Email already registered` | PASS |
| 3 | Register invalid email | 400 | 400 `email must be valid` | PASS |
| 4 | Register missing `name` | 400 | 400 `name is required` | PASS |
| 5 | Register unknown role (`SUPER`) | 400 | 400 `Invalid request` | PASS |
| 6 | Register as `EXPERT` | 201 | 201, role persisted | PASS |
| 7 | `GET /api/users` | 200 list | 200 list, no passwords leaked | PASS |
| 8 | `GET /api/users/email/{UPPER}` | 200 | 200, email normalised to lower case | PASS |
| 9 | `GET /api/users/999999` | 404 | 404 `User not found with id: 999999` | PASS |

## 2. Part 2 — Question API results

Owner user id `5`, non-owner id `6`.

| # | Request | Expected | Actual | Status |
|---|---------|----------|--------|--------|
| 1 | `POST /api/questions` (valid, `X-User-Id: 5`) | 201 | 201, `askerId`/`askerName` populated | PASS |
| 2 | `POST` without `X-User-Id` | 400 | 400 `Header X-User-Id is required` | PASS |
| 3 | `POST` bad category | 400 | 400 `Invalid request` | PASS |
| 4 | `POST` short title | 400 | 400 `title must be between 5 and 200 characters` | PASS |
| 5 | `POST` unknown asker id | 404 | 404 `User not found with id` | PASS |
| 6 | `GET /api/questions` | 200 | 200 | PASS |
| 7 | `GET /api/questions/{id}` missing | 404 | 404 `Question not found with id` | PASS |
| 8 | `GET /api/questions/category/PROGRAMMING` | 200 | 200 filtered list | PASS |
| 9 | `GET /api/questions/category/SPACE` | 400 | 400 `Invalid request` | PASS |
| 10 | `GET /api/questions/user/5` | 200 | 200 filtered list | PASS |
| 11 | `PUT /api/questions/2` as non-owner | 403 | 403 `Only the owner of this question can modify it` | PASS |
| 12 | `PUT /api/questions/2` without header | 400 | 400 | PASS |
| 13 | `PUT` missing question | 404 | 404 | PASS |
| 14 | `PUT /api/questions/2` as owner | 200 | 200, `updatedAt` advanced, `createdAt` unchanged | PASS |
| 15 | `DELETE /api/questions/2` as non-owner | 403 | 403 | PASS |
| 16 | `DELETE /api/questions/2` without header | 400 | 400 | PASS |
| 17 | `DELETE /api/questions/2` as owner | 204 | 204 | PASS |
| 18 | `GET /api/questions/2` after delete | 404 | 404 | PASS |

Smoke: `GET /hello` → `Hello` (200); `GET /greeting` → `Welcome to ExpertConnect!` (200).

## 3. Confirmed behaviour

- Bean-validation errors return a consistent `{timestamp,status,error,message,errors[]}` body.
- A missing `X-User-Id` header is handled by `GlobalExceptionHandler` (not a raw 500).
- Unknown enum values for `category`/`role` produce 400, not 500.
- Email is trimmed and lower-cased before uniqueness checks and lookups.
- Question read endpoints return DTOs, so the `User.password` field is never serialised.
- Ownership is checked *before* the mutation in both `update` and `delete`.

## 4. Findings and recommendations

1. **Plaintext passwords (high).** `UserService.register` stores
   `request.password()` as-is and there is no encoder. Even though the password
   is not returned, the DB stores it in the clear. Add a `PasswordEncoder`
   (BCrypt) now or in Part 4 with Spring Security.
2. **No password strength rule (medium).** `RegisterRequest.password` only has
   `@NotBlank`. Add a minimum length (e.g. `@Size(min = 8)`).
3. **Trust-based identity (medium, known/temporary).** `X-User-Id` is a
   caller-supplied header with no verification, so any client can impersonate
   any user id. The code comment already flags this for Part 4 (Spring Security).
4. **Public user directory (medium).** `GET /api/users` and `/api/users/{id}`
   expose every user's name and email with no auth. Restrict in Part 4.
5. **`X-User-Id` non-numeric.** A header like `abc` currently produces a 400
   `Invalid request` via `MethodArgumentTypeMismatchException`, which is
   acceptable, but the message does not name the offending header.
6. **Tracked log files (low).** `app.log` and `app-err.log` were committed to
   the repository. They are build/run artefacts and should not be versioned;
   `.gitignore` now ignores `*.log` and `.freebuff/`, and both files are
   untracked in this commit.
7. **`spring.jpa.show-sql=true` (low).** Fine for a college project; turn off
   for production to avoid verbose logging.

## 5. Conclusion

All 27 functional Part 1 + Part 2 cases behaved as specified. No blocker or
regression was found. The remaining items are security hardening tasks that
belong to Part 4, plus the two small hygiene fixes (log ignore rules and the
password-strength/encoder follow-up).

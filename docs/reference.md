# WashQ -- Reference Tables

## Database Table

### Table: `users`

| Column | Type | Constraints | Description |
|---|---|---|---|
| id | BIGSERIAL | PRIMARY KEY | Auto-incrementing user ID |
| email | VARCHAR(255) | UNIQUE, NOT NULL | Primary login identifier |
| password_hash | VARCHAR(255) | NULLABLE | BCrypt hashed password (null for OAuth users) |
| first_name | VARCHAR(100) | NOT NULL | User's first name |
| last_name | VARCHAR(100) | NOT NULL | User's last name |
| address | TEXT | NULLABLE | Home or delivery address |
| contact_number | VARCHAR(20) | NULLABLE | Philippine mobile number |
| role | VARCHAR(20) | NOT NULL, default 'CUSTOMER' | CUSTOMER / STAFF / ADMIN |
| profile_picture_url | VARCHAR(500) | NULLABLE | Path to uploaded avatar |
| oauth_provider | VARCHAR(50) | NULLABLE | e.g. 'google' |
| oauth_id | VARCHAR(255) | NULLABLE | Google subject ID |
| created_at | TIMESTAMP | NOT NULL, set by @PrePersist | Account creation time |
| updated_at | TIMESTAMP | NOT NULL, set by @PreUpdate | Last updated time |

## API Endpoints

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | /auth/register | None | Register a new user account |
| POST | /auth/login | None | Login with email and password |
| GET | /auth/me | JWT Bearer | Get current authenticated user |
| POST | /auth/logout | JWT Bearer | End session |

## Implementation Summary

### User Registration

WashQ allows new users to create an account via `POST /auth/register`.

**Registration fields:**

| Field | Backend Validation | Frontend Validation | Notes |
|---|---|---|---|
| firstName | `@NotBlank` | Required | -- |
| lastName | `@NotBlank` | Required | -- |
| email | `@NotBlank`, `@Email` | Required, format check | Must be valid email format |
| password | `@NotBlank`, `@Size(min=8)` | Required, min 8 chars | -- |
| address | Optional (nullable) | Required | Home or delivery address |
| contactNumber | Optional (nullable) | Required | Philippine mobile number |

**Validation process:**
Input is validated using Jakarta Bean Validation annotations (`@NotBlank`, `@Email`, `@Size`) on the request DTO. The `@Valid` annotation in the controller triggers validation before the request reaches the service layer. Any failed constraint returns `400 Bad Request` with error code `VALID-001`. A `GlobalExceptionHandler` (`@ControllerAdvice`) catches all exceptions and returns a standardized JSON error response.

**How duplicate accounts are prevented:**
Before creating a new user, `AuthService` calls `UserRepository.existsByEmail(email)`. If the email is already registered, a `DuplicateResourceException` is thrown and mapped to `409 Conflict` with error code `DB-002`. The email column also has a `UNIQUE` constraint at the database level as a final safeguard.

**How passwords are stored securely:**
Passwords are never stored in plain text. The raw password is immediately hashed using `BCryptPasswordEncoder` with a cost factor of 12. The resulting hash (e.g., `$2a$12$...`) is stored in the `password_hash` column. The original password is discarded and never persisted. The `UserDTO` excludes the password field from all API responses.

---

### User Login

WashQ allows registered users to log in via `POST /auth/login`.

**Login credentials:**

| Field | Description |
|---|---|
| email | The registered account email |
| password | The plain-text password (compared against the stored BCrypt hash) |

**How the system verifies users:**

1. `AuthService.login()` calls `UserRepository.findByEmail(email)` to look up the user. If no user is found, an `InvalidCredentialsException` is thrown immediately.
2. `PasswordEncoder.matches(rawPassword, user.getPasswordHash())` is called to compare the submitted password against the stored BCrypt hash without decrypting it.
3. If the password does not match, an `InvalidCredentialsException` is thrown and mapped to `401 Unauthorized` with error code `AUTH-001`.

**What happens after successful login:**

1. `JwtUtil.generateToken(id, email, role)` creates a signed JWT access token.
2. The system returns `200 OK` with a `UserDTO` (`id`, `email`, `firstName`, `lastName`, `role` -- no password) and the JWT access token.
3. The client stores the JWT in `localStorage` and attaches it as a `Bearer` token in the `Authorization` header on all subsequent requests.
4. The `JwtAuthFilter` validates the token on every protected request and populates the Spring Security context.

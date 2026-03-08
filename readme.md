# DDD Library — Project Documentation

> A beginner-friendly guide to understanding this project, its architecture, and the concepts behind it.

---

## Table of Contents

1. [What Is This Project?](#1-what-is-this-project)
2. [Technologies Used](#2-technologies-used)
3. [Domain Driven Design — A Primer](#3-domain-driven-design--a-primer)
4. [Spring Framework — A Primer](#4-spring-framework--a-primer)
5. [Project Structure Overview](#5-project-structure-overview)
6. [The Security Module](#6-the-security-module)
7. [The Catalog Module](#7-the-catalog-module)
8. [The Lending Module](#8-the-lending-module)
9. [Cross-Cutting Concerns](#9-cross-cutting-concerns)
10. [How the Modules Communicate](#10-how-the-modules-communicate)
11. [API Reference](#11-api-reference)
12. [Running the Project](#12-running-the-project)
13. [Testing Strategy](#13-testing-strategy)
14. [Glossary](#14-glossary)

---

## 1. What Is This Project?

This project is a **Library Management System**, started from the code used to support this [Spring IO 2024 talk](https://2024.springio.net/sessions/implementing-domain-driven-design-with-spring/): "Implementing Domain Driven Design with Spring". It is built as a teaching example of **Domain Driven Design (DDD)** using the **Spring Boot** framework. It manages three core workflows:

- **Catalog management** — Adding books and physical copies to the library
- **Lending** — Renting out and returning books
- **Authentication** — Controlling who can do what in the system

Think of it like the backend of a real library's digital system — the kind of software that runs when a librarian scans a book or a patron checks out a title at the front desk.

---

## 2. Technologies Used

| Technology | Purpose |
|---|---|
| **Java 21** | The programming language |
| **Spring Boot 3.3** | The application framework |
| **Spring Data JPA** | Database access (talking to the DB without writing SQL) |
| **Spring Security** | Authentication and authorization |
| **H2 Database** | A lightweight in-memory/file database for development |
| **JWT (JSON Web Tokens)** | Stateless user authentication tokens |
| **Gradle** | Build tool (compiles and packages the project) |
| **Spring Modulith** | Enforces module boundaries within the application |
| **JUnit 5 + Mockito** | Testing framework |
| **Open Library API** | External API used to look up book information by ISBN |

---

## 3. Domain Driven Design — A Primer

Domain Driven Design (DDD) is a way of structuring software so that the code closely mirrors the **real-world problem domain** it models. In plain terms: the code should speak the language of the business.

### 3.1 The Ubiquitous Language

In DDD, developers and domain experts (e.g., librarians, business analysts) agree on a shared vocabulary. In this project:

- A **Book** is a title that exists in the catalog (e.g., *Clean Code*).
- A **Copy** is a physical instance of a book with a barcode (the actual object on the shelf).
- A **Loan** represents the act of a patron borrowing a specific copy.
- A **Patron** is a library member who can borrow books.
- A **Librarian** can add books and copies to the system.

The code uses these exact words as class names — `Book`, `Copy`, `Loan` — not generic terms like `Item` or `Record`.

### 3.2 Bounded Contexts

A **Bounded Context** is a logical boundary within the system where a concept has a specific, well-defined meaning. This project has three bounded contexts:

- **Security** — who you are and what you can do
- **Catalog** — what books and copies exist
- **Lending** — which copies are borrowed and by whom

Each context lives in its own Java package and knows as little as possible about the others. This separation prevents one part of the system from becoming tightly coupled to another.

### 3.3 Aggregates and Aggregate Roots

An **Aggregate** is a cluster of related objects that are always kept consistent together. The **Aggregate Root** is the main object in that cluster — the one that controls access to all the others.

In this project:
- **`Book`** is an aggregate root. You cannot directly manipulate a `Copy` without going through business logic.
- **`Loan`** is an aggregate root. It controls its own lifecycle (creation, return).

**Why does this matter?** It ensures that business rules are enforced in one place. For example, a `Loan` can only be created if the copy is available — that check lives inside the `Loan` class itself, not scattered across the application.

### 3.4 Value Objects

A **Value Object** is an immutable object defined entirely by its value, not by an identity. Two value objects with the same value are considered equal.

Examples in this project:

| Class | What it represents |
|---|---|
| `Isbn` | An ISBN number (e.g., `9780132350884`) — validates that it's a real ISBN; serves as the domain identity of a `Book` |
| `BarCode` | A physical barcode string on a book copy |
| `CopyId` | A UUID that uniquely identifies a copy |
| `LoanId` | A UUID that uniquely identifies a loan |
| `UserId` | A UUID that uniquely identifies a user |

Value objects are **not entities** — they have no database identity of their own. They are embedded within entities.

**Why immutability?** If an ISBN is `9780132350884`, it should never change. Wrapping it in an immutable `Isbn` object ensures this is enforced by the compiler, not just by convention.

### 3.5 Domain Events

A **Domain Event** is something that happened in the domain that other parts of the system might care about. They are named in the past tense.

In this project:

- `LoanCreated` — fired when a patron borrows a book
- `LoanClosed` — fired when a patron returns a book

The **Lending** module fires these events. The **Catalog** module listens to them and updates copy availability accordingly. This means neither module directly calls the other — they communicate via events.

**Why is this useful?** Loose coupling. If you later add a notification module that emails patrons on loan creation, you simply add another listener for `LoanCreated` — no changes needed in the lending module.

### 3.6 Repositories

A **Repository** is an abstraction that makes domain objects feel like they live in an in-memory collection, hiding all the complexity of database operations.

Instead of writing SQL like:
```sql
SELECT * FROM book WHERE isbn = '9780132350884'
```

You write:
```java
bookRepository.findByIsbnValue("9780132350884")
```

Repositories in DDD belong to the **domain layer** (as interfaces) but are implemented in the **infrastructure layer**.

### 3.7 Use Cases (Application Services)

A **Use Case** describes a specific piece of business functionality from the user's perspective. Each use case is a single class with a single `execute()` method.

Examples:
- `AddBookToCatalogUseCase` — "As a librarian, I want to add a book using its ISBN"
- `RentBookUseCase` — "As a patron, I want to borrow a copy"
- `ReturnBookUseCase` — "As a patron, I want to return a copy"

This makes the application's capabilities explicit and easy to discover just by looking at the class names.

### 3.8 The Layered Architecture

DDD is typically implemented with a layered architecture:

```
┌─────────────────────────────────┐
│       Presentation Layer        │  REST controllers, HTTP handling
├─────────────────────────────────┤
│       Application Layer         │  Use cases, orchestration logic
├─────────────────────────────────┤
│         Domain Layer            │  Entities, value objects, events, repos (interfaces)
├─────────────────────────────────┤
│     Infrastructure Layer        │  DB implementations, external APIs, JPA
└─────────────────────────────────┘
```

Dependencies point **inward only**: presentation depends on application, application depends on domain, infrastructure implements domain interfaces. The domain layer has **zero external dependencies**.

---

## 4. Spring Framework — A Primer

### 4.1 What is Spring Boot?

Spring Boot is an opinionated application framework for Java that handles the boilerplate of setting up a web server, database connections, security, and more — so you can focus on writing business logic.

When you run this project, Spring Boot automatically:
- Starts an embedded Tomcat web server on port 8080
- Sets up database connectivity
- Enables security filters
- Wires together all the application components

### 4.2 Dependency Injection and the IoC Container

Spring's core concept is **Inversion of Control (IoC)**: instead of your code creating its own dependencies, Spring creates and provides them.

You mark a class with annotations like `@Service`, `@Repository`, or `@Component`, and Spring automatically:
- Creates an instance of that class (a "bean")
- Injects it wherever it's needed via `@Autowired` or constructor injection

For example, `AddBookToCatalogUseCase` needs a `BookSearchService` and a `BookRepository`. Spring creates both and passes them into the constructor — you never call `new`.

### 4.3 Spring Data JPA

**JPA (Java Persistence API)** is a standard for mapping Java objects to database tables. **Spring Data JPA** makes this even simpler by generating common database queries automatically.

When you write:
```java
interface BookRepository extends CrudRepository<Book, Long> {
    Optional<Book> findByIsbnValue(String isbn);
}
```

Spring Data JPA **generates the SQL query automatically** by parsing the method name. No SQL needed.

### 4.4 Spring Security

Spring Security is the authentication and authorization framework. It intercepts all HTTP requests and checks:

1. **Authentication** — Is the user who they claim to be? (verified via JWT token)
2. **Authorization** — Is the authenticated user allowed to perform this action? (checked via roles)

### 4.5 AOP — Aspect Oriented Programming

**AOP** allows you to add behaviour to methods without modifying those methods. It is used for cross-cutting concerns like logging, timing, or transaction management.

In this project, `UseCaseLoggingAdvice` intercepts every use case execution and logs the method name, parameters, and execution time — without a single log statement in the use case classes themselves.

### 4.6 REST Controllers

A **REST Controller** maps HTTP requests to Java methods. For example:

```java
@PostMapping("/loans")
@ResponseStatus(HttpStatus.CREATED)
public void rentBook(@RequestBody RentRequest request) {
    rentBookUseCase.execute(request.copyId(), userId);
}
```

- `@PostMapping` — handles HTTP POST requests at `/loans`
- `@RequestBody` — deserializes the JSON request body into a Java object
- `@ResponseStatus` — sets the HTTP status code of the response

---

## 5. Project Structure Overview

```
src/main/java/library/
│
├── LibraryApplication.java       ← Application entry point
├── Bootstrap.java                ← Seeds demo data on startup (dev profile only)
├── UseCase.java                  ← Custom annotation that marks use case classes
├── UseCaseLoggingAdvice.java     ← AOP aspect: logs all use case executions
│
├── security/                     ← BOUNDED CONTEXT: Authentication & Authorization
│   ├── AuthController.java       ← POST /auth/login endpoint
│   ├── SecurityConfig.java       ← Security rules for every endpoint
│   ├── application/
│   │   ├── JwtService.java       ← Token generation and validation
│   │   ├── LibraryUserDetailsService.java ← Loads user from DB for Spring Security
│   │   ├── LoginRequest.java     ← DTO: { username, password }
│   │   └── TokenResponse.java    ← DTO: { token }
│   ├── domain/
│   │   ├── Role.java             ← Enum: ADMIN, LIBRARIAN, PATRON
│   │   ├── User.java             ← User entity (stored in DB)
│   │   └── UserRepository.java   ← Finds users by username
│   └── infrastructure/
│       └── JwtAuthenticationFilter.java ← Reads JWT from each HTTP request
│
├── catalog/                      ← BOUNDED CONTEXT: Books and Copies
│   ├── CatalogController.java    ← GET/POST /catalog/books, POST /catalog/copies
│   ├── application/
│   │   ├── AddBookToCatalogUseCase.java  ← Looks up book by ISBN + saves it
│   │   ├── RegisterBookCopyUseCase.java  ← Creates a physical copy
│   │   ├── ListBooksUseCase.java         ← Lists/searches books
│   │   ├── BookSearchService.java        ← Interface for ISBN lookup
│   │   ├── BookInformation.java          ← DTO: { title }
│   │   └── DomainEventListener.java      ← Listens to LoanCreated/LoanClosed events
│   ├── domain/
│   │   ├── Book.java             ← Aggregate root: title + ISBN (ISBN is the domain identity)
│   │   ├── Isbn.java             ← Value object: validated ISBN string; identifies a Book
│   │   ├── BookRepository.java   ← Find/save books
│   │   ├── Copy.java             ← Physical copy entity
│   │   ├── CopyId.java           ← Value object: UUID
│   │   ├── BarCode.java          ← Value object: barcode string
│   │   └── CopyRepository.java   ← Find/save copies
│   └── infrastructure/
│       ├── OpenLibraryBookSearchService.java  ← Calls openlibrary.org API
│       └── OpenLibraryIsbnSearchResult.java   ← JSON response DTO
│
└── lending/                      ← BOUNDED CONTEXT: Loans
    ├── LendingController.java    ← POST /loans, POST /loans/{id}/return
    ├── application/
    │   ├── RentBookUseCase.java  ← Creates a loan
    │   └── ReturnBookUseCase.java← Closes a loan
    └── domain/
        ├── Loan.java             ← Aggregate root: loan lifecycle
        ├── LoanId.java           ← Value object: UUID
        ├── LoanCreated.java      ← Domain event (fired on loan creation)
        ├── LoanClosed.java       ← Domain event (fired on loan return)
        ├── LoanRepository.java   ← Find/save loans, check availability
        ├── CopyId.java           ← Value object (mirror of catalog's CopyId)
        └── UserId.java           ← Value object: user UUID
```

### Why Is `CopyId` Duplicated?

You'll notice `CopyId` exists in both `catalog/domain` and `lending/domain`. This is **intentional** in DDD. Each bounded context should be self-contained and not import classes from another context's domain. The `lending` module has its own `CopyId` that it uses as a reference — it doesn't know or care about the `Copy` entity itself.

---

## 6. The Security Module

### 6.1 Roles and Permissions

The system defines three roles:

| Role | Permissions |
|---|---|
| `ADMIN` | Can do everything |
| `LIBRARIAN` | Can add books and copies to the catalog |
| `PATRON` | Can rent and return books |

### 6.2 The User Entity

Users are stored in the database table `library_user`. Each user has:
- A **UUID** (`userId`) — the domain identity used in JWT tokens
- A **username** — unique login name
- A **password** — stored as a BCrypt hash (never plain text)
- A set of **roles**

### 6.3 How Authentication Works

```
Client                         Server
  │                              │
  │   POST /auth/login           │
  │   { username, password }     │
  │ ──────────────────────────► │
  │                              │ 1. Look up user by username
  │                              │ 2. Verify BCrypt password
  │                              │ 3. Generate JWT token with userId + roles
  │   { token: "eyJ..." }        │
  │ ◄────────────────────────── │
  │                              │
  │   POST /loans                │
  │   Authorization: Bearer eyJ  │
  │ ──────────────────────────► │
  │                              │ 4. JwtAuthenticationFilter runs
  │                              │ 5. Validate token signature + expiry
  │                              │ 6. Extract userId and roles
  │                              │ 7. Set authentication in Spring context
  │                              │ 8. Proceed with request
  │   201 Created                │
  │ ◄────────────────────────── │
```

### 6.4 JWT Tokens

A **JWT (JSON Web Token)** is a compact, self-contained token that contains:
- **Subject** — the user's UUID
- **Claims** — the user's roles
- **Expiry** — when the token stops being valid (1 hour)
- **Signature** — a cryptographic signature that ensures it hasn't been tampered with

The server never needs to look up a session in the database — it just validates the token's signature. This is called **stateless authentication**.

### 6.5 The Security Filter Chain

Spring Security processes every incoming HTTP request through a chain of filters. The custom `JwtAuthenticationFilter` runs before any controller is invoked:

1. Reads the `Authorization: Bearer <token>` header
2. Validates the token with `JwtService`
3. Extracts the user's UUID and roles
4. Stores the authentication in the `SecurityContextHolder`
5. Passes the request forward to the controller

If the token is missing or invalid, the request proceeds unauthenticated — the authorization rules in `SecurityConfig` will then reject it for protected endpoints.

---

## 7. The Catalog Module

### 7.1 The Book Aggregate

A `Book` is the central aggregate root of the catalog context. It has:
- A `title` string
- An `Isbn` value object — this is the domain identity of the book

**ISBN as domain identity.** In the real world, an ISBN is the globally recognised, unique identifier for a book title. Using it directly as the domain identity makes the model more honest — there is no artificial surrogate UUID, just the natural key that the domain already provides. The database still uses an internal `pk` column for physical storage, but that is an infrastructure detail hidden behind the repository.

### 7.2 The Isbn Value Object

The `Isbn` class is not just a `String`. It wraps the string and **validates** it on construction using the Apache Commons ISBNValidator. If you try to create an `Isbn` with an invalid value, you get an immediate `IllegalArgumentException`.

```java
new Isbn("9780132350884")  // valid — OK
new Isbn("1234567890123")  // invalid — throws exception immediately
```

This means invalid ISBNs **cannot exist** in the domain model. You never need to check "is this ISBN valid?" later in the code — if you have an `Isbn` object, it's already valid.

### 7.3 The Copy Entity

A `Copy` represents a single physical book on a shelf. It has:
- A `CopyId` (UUID)
- An `Isbn` — identifies which book title this copy belongs to
- A `BarCode` (the sticker on the physical book)
- An `available` flag

The copy knows whether it is currently available for borrowing. When a loan is created or closed, the catalog's `DomainEventListener` updates this flag.

### 7.4 Adding a Book — Walk-Through

When a librarian calls `POST /catalog/books` with an ISBN:

```
CatalogController.addBook(isbn)
    └─► AddBookToCatalogUseCase.execute(isbn)
            ├─► OpenLibraryBookSearchService.search(isbn)
            │       └─► GET https://openlibrary.org/isbn/{isbn}.json
            │               returns { title: "Clean Code" }
            ├─► new Book("Clean Code", isbn)
            └─► bookRepository.save(book)
```

1. The controller receives the ISBN from the HTTP request
2. The use case is called
3. The use case calls the `BookSearchService` to look up the book's title on the Open Library API
4. A new `Book` entity is created with the title and ISBN
5. It is saved to the database

### 7.5 The DomainEventListener

This class in the catalog module listens for events fired by the lending module:

```java
@TransactionalEventListener
void handle(LoanCreated event) {
    Copy copy = copyRepository.findByCopyId(event.copyId());
    copy.makeUnavailable();
    copyRepository.save(copy);
}

@TransactionalEventListener
void handle(LoanClosed event) {
    Copy copy = copyRepository.findByCopyId(event.copyId());
    copy.makeAvailable();
    copyRepository.save(copy);
}
```

The `@TransactionalEventListener` annotation ensures the event handler runs in the same database transaction as the operation that triggered the event, guaranteeing consistency.

---

## 8. The Lending Module

### 8.1 The Loan Aggregate

`Loan` is the aggregate root of the lending context. It has:
- A `LoanId` (UUID)
- A `CopyId` — which copy was borrowed
- A `UserId` — who borrowed it
- `createdAt` — when the loan was created
- `expectedReturnDate` — 30 days after creation
- `returnedAt` — when the copy was returned (null if still on loan)
- A `version` field for optimistic locking

### 8.2 Business Rules Enforced by the Aggregate

The `Loan` constructor enforces this rule:
> A loan can only be created if the copy is currently available.

```java
public Loan(CopyId copyId, UserId userId, LoanRepository loanRepository) {
    if (!loanRepository.isAvailable(copyId)) {
        throw new IllegalArgumentException("Copy is not available");
    }
    // ... create the loan
    registerEvent(new LoanCreated(copyId));
}
```

Notice that the `Loan` entity itself checks availability via the repository. This might look unusual — usually entities don't reference repositories — but it's a deliberate design choice to keep the business rule *inside the aggregate* rather than scattering it across use case classes.

### 8.3 Optimistic Locking

The `@Version` field prevents **lost updates** in concurrent scenarios. If two patrons try to borrow the same copy at exactly the same time:

1. Both read the `Loan` count as 0 (available)
2. Both try to save a new `Loan`
3. The first save succeeds and increments the `version`
4. The second save fails because the `version` it read is now stale

This prevents double-booking without using database-level locks.

### 8.4 Returning a Book — Walk-Through

When a patron calls `POST /loans/{loanId}/return`:

```
LendingController.returnBook(loanId)
    └─► ReturnBookUseCase.execute(loanId)
            ├─► loanRepository.findByLoanId(loanId)
            └─► loan.returned()
                    └─► registerEvent(new LoanClosed(copyId))
                            └─► DomainEventListener.handle(LoanClosed)
                                    └─► copy.makeAvailable()
```

The `LoanClosed` event travels from the lending module to the catalog module, which updates the copy's availability — all within a single database transaction.

---

## 9. Cross-Cutting Concerns

### 9.1 The `@UseCase` Annotation

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service
@Validated
public @interface UseCase {}
```

This is a **custom annotation** that combines `@Service` (makes it a Spring-managed bean) and `@Validated` (enables Bean Validation). Any class annotated with `@UseCase` is automatically:
- Registered as a Spring service
- Subject to method-level validation
- Intercepted by the logging AOP advice

### 9.2 The Use Case Logging Advice

`UseCaseLoggingAdvice` uses Spring AOP to intercept every method in every `@UseCase` class:

```java
@Around("@within(library.UseCase)")
public Object logUseCaseExecution(ProceedingJoinPoint joinPoint) throws Throwable {
    StopWatch watch = new StopWatch();
    watch.start();
    Object result = joinPoint.proceed();
    watch.stop();
    log.info("{} executed in {}ms with args {}",
        joinPoint.getSignature().getName(),
        watch.getTotalTimeMillis(),
        Arrays.toString(joinPoint.getArgs()));
    return result;
}
```

Without touching any use case class, every use case execution is logged with timing information. This is the power of AOP for cross-cutting concerns.

### 9.3 The Bootstrap Component

`Bootstrap` implements `ApplicationRunner` and is annotated with `@Profile("dev")`. It only runs when the `dev` Spring profile is active (using `./dev.sh`).

It seeds the database with:
- 3 books (fetched from Open Library by ISBN)
- 2 copies per book
- 3 users (admin, librarian, patron)

This allows developers to immediately test the API without manually setting up data.

---

## 10. How the Modules Communicate

The three bounded contexts communicate in carefully controlled ways:

```
┌───────────────┐        events        ┌───────────────┐
│   LENDING     │ ──────────────────► │   CATALOG     │
│               │  LoanCreated         │               │
│  - Loan       │  LoanClosed          │  - Book       │
│  - LoanRepo   │                      │  - Copy       │
└───────────────┘                      └───────────────┘
        │                                      │
        │ reads userId                         │ reads copyId
        │ from JWT                             │ from event
        │                                      │
        └──────────────┬───────────────────────┘
                       │
               ┌───────────────┐
               │   SECURITY    │
               │               │
               │  - User       │
               │  - JWT        │
               └───────────────┘
```

**Key principle:** The Lending module does NOT import anything from the Catalog module's domain. It only holds a `CopyId` (a UUID reference). Similarly, the Catalog module does NOT know about Loans — it only reacts to events.

This loose coupling means:
- You could replace the Catalog module without touching the Lending module
- You could add new modules (e.g., Notifications) that listen to `LoanCreated` without changing any existing code

---

## 11. API Reference

### Authentication

#### POST /auth/login
Authenticate and receive a JWT token.

**Request body:**
```json
{
  "username": "patron",
  "password": "patron123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

Use the token in subsequent requests as `Authorization: Bearer <token>`.

---

### Catalog

#### GET /catalog/books
List all books. Optionally filter by title or ISBN.

**Query parameters:**
- `title` — partial, case-insensitive title search
- `isbn` — exact ISBN search

**Response (200 OK):**
```json
[
  {
    "title": "Clean Code",
    "isbn": "9780132350884"
  }
]
```

*No authentication required.*

---

#### POST /catalog/books
Add a new book to the catalog (fetches title from Open Library).

**Request body:**
```json
{
  "isbn": "9780132350884"
}
```

**Response:** `201 Created`

*Requires LIBRARIAN or ADMIN role.*

---

#### POST /catalog/copies
Register a physical copy of a book.

**Request body:**
```json
{
  "isbn": "9780132350884",
  "barCode": "LIB-0001"
}
```

**Response:** `201 Created`

*Requires LIBRARIAN or ADMIN role.*

---

### Lending

#### POST /loans
Borrow a copy of a book.

**Request body:**
```json
{
  "copyId": "6ba7b810-9dad-11d1-80b4-00c04fd430c8"
}
```

**Response:** `201 Created`

*Requires PATRON or ADMIN role. The user is identified from the JWT token.*

---

#### POST /loans/{loanId}/return
Return a borrowed copy.

**Path parameter:** `loanId` — UUID of the loan

**Response:** `204 No Content`

*Requires PATRON or ADMIN role.*

---

## 12. Running the Project

### Prerequisites
- Java 21 (JDK)
- Internet access (for Open Library API calls during book addition)

### Scripts

| Script | Command | Description |
|---|---|---|
| `build.sh` | `./build.sh` | Compile and run tests |
| `dev.sh` | `./dev.sh` | Run with demo data (dev profile) |
| `run.sh` | `./run.sh` | Run in production mode (no demo data) |

### First Steps

1. Start with dev profile: `./dev.sh`
2. The H2 console is available at `http://localhost:8080/h2-console`
   - JDBC URL: `jdbc:h2:file:./data/library`
   - Username: `sa`, Password: *(empty)*
3. Log in via API:
   ```bash
   curl -X POST http://localhost:8080/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"patron","password":"patron123"}'
   ```
4. Use the returned token for authenticated requests

### Demo Users (dev profile only)

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `librarian` | `librarian123` | LIBRARIAN |
| `patron` | `patron123` | PATRON |

---

## 13. Testing Strategy

The project uses **unit tests** at the domain layer — testing business rules in isolation without a running database or server.

### What Is Tested

| Test Class | What It Tests |
|---|---|
| `IsbnTest` | ISBN validation — valid/invalid values |
| `BookTest` | Book creation — required fields, ISBN as identity |
| `CopyTest` | Copy lifecycle — availability state changes |
| `LoanTest` | Loan business rules — availability check, domain events |

### The Loan Test Uses Mocking

The `LoanTest` uses **Mockito** to fake the `LoanRepository`:

```java
LoanRepository repo = mock(LoanRepository.class);
when(repo.isAvailable(copyId)).thenReturn(false);

assertThrows(IllegalArgumentException.class,
    () -> new Loan(copyId, userId, repo));
```

This tests the business rule ("cannot create a loan if copy is unavailable") without needing a real database.

### Testing Philosophy in DDD

In DDD, the most important tests are **domain tests** — they verify that business rules are enforced by the domain objects themselves. If the `Loan` aggregate is correct, any use case or controller that uses it will inherit that correctness.

---

## 14. Glossary

| Term | Definition |
|---|---|
| **Aggregate** | A cluster of domain objects treated as a unit; has one aggregate root |
| **Aggregate Root** | The main entity in an aggregate; the only entry point for mutations |
| **AOP** | Aspect Oriented Programming — adds behaviour to methods without modifying them |
| **BCrypt** | A strong hashing algorithm used to store passwords securely |
| **Bean** | A Spring-managed object instance |
| **Bounded Context** | A logical boundary in which domain terms have a specific meaning |
| **DTO** | Data Transfer Object — a simple container for moving data between layers |
| **Domain Event** | An immutable record of something that happened in the domain |
| **Entity** | A domain object with a unique identity that persists over time |
| **IoC** | Inversion of Control — Spring creates and wires your objects for you |
| **ISBN** | International Standard Book Number — a unique identifier for books |
| **JPA** | Java Persistence API — standard for mapping Java objects to database tables |
| **JWT** | JSON Web Token — a self-contained, signed token used for authentication |
| **Optimistic Locking** | A concurrency strategy using a version number to detect conflicting updates |
| **Repository** | Abstraction over data storage — makes persistence look like a collection |
| **Stateless Authentication** | Server holds no session; every request carries its own credentials (JWT) |
| **Ubiquitous Language** | Shared vocabulary between developers and domain experts, reflected in code |
| **Use Case** | A single, well-defined piece of application behaviour from the user's perspective |
| **Value Object** | An immutable domain object with no identity, defined only by its value |

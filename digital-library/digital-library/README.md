# Digital Library Management System

Spring Boot 3 · Thymeleaf · Spring Security · Spring Data JPA · MySQL

## 1. Prerequisites
- JDK 17+
- Maven (or use your IDE's built-in Maven)
- MySQL 8 running locally
- IDE with the **Lombok** plugin enabled (IntelliJ: Settings > Build > Compiler > Annotation Processors > Enable)

## 2. Run
1. Open `src/main/resources/application.properties` and set your MySQL password.
   The database `library_db` is created automatically.
2. From this folder: `mvn spring-boot:run`  (or run `LibraryApplication` from the IDE)
3. Open http://localhost:8080

Tables are created by Hibernate on first start. A default admin and six sample books are inserted if the database is empty.

| Role  | Login                                  |
|-------|----------------------------------------|
| Admin | admin@library.com / admin123           |
| User  | click "Create an account" on the login page |

## 3. Business rules
- Loan period: **14 days** and fine: **₹5 per overdue day** (constants in `model/Loan.java`)
- A member with an **unpaid fine cannot issue** new books
- A member cannot issue the same book twice at once
- **Advance booking** is only possible when no copy is available. Copies that become free are held for earlier reservations; a member's own reservation is marked FULFILLED when they issue the book
- A book with loan or reservation history cannot be deleted (set quantity to 0 instead)

## 4. Database design
| Table | Key columns |
|-------|-------------|
| users | id, name, email (unique), password (BCrypt), role (ADMIN/USER), enabled, created_at |
| book | id, title, author, isbn (unique), category, total_quantity, available_quantity |
| loan | id, book_id → book, user_id → users, issue_date, due_date, return_date (NULL = still issued), fine_amount, fine_paid |
| reservation | id, book_id → book, user_id → users, reserved_on, status (ACTIVE/FULFILLED/CANCELLED) |
| contact_message | id, user_id → users, subject, message, created_at |

## 5. Project layout
```
model/       entities (User, Book, Loan, Reservation, ContactMessage)
repository/  Spring Data JPA interfaces (queries)
service/     LibraryService - all business rules (issue, return, fines, reserve)
controller/  AuthController, UserController, AdminController
config/      SecurityConfig - login, roles, URL protection
templates/   Thymeleaf pages (admin/ = admin panel)
```

## 6. Testing fines without waiting 2 weeks
Issue a book as a user, then in MySQL:
```sql
UPDATE loan SET due_date = DATE_SUB(CURDATE(), INTERVAL 5 DAY) WHERE id = 1;
```
Now "My Books" shows 5 days overdue (₹25). Return the book, then mark the fine paid from Admin > Fines.

## 7. Suggested demo flow
1. Admin: add a book with quantity 1 → Members/Issued/Fines/Messages pages
2. User A registers, issues the book. User B registers, **reserves** it
3. User A returns it (late, using the SQL trick) → fine appears; admin marks it paid
4. User B issues the book (reservation becomes FULFILLED)
5. User B sends a message via Contact → admin reads it under Messages

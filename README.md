# Blog REST API

A RESTful API for a blog application built with Spring Boot, featuring JWT authentication, post and comment management, and interactive API documentation via Swagger UI.

## Features

- **User Authentication** — Register and login with JWT token-based security
- **Blog Post Management** — Full CRUD operations for blog posts with category and tag support
- **Comment Management** — Full CRUD operations for comments on posts
- **Pagination & Sorting** — Paginated responses for post listings
- **Interactive API Documentation** — Swagger UI with JWT authentication support
- **Input Validation** — Request validation with descriptive error messages

## Technology Stack

| Technology | Version |
|---|---|
| Java | 21 |
| Spring Boot | 4.0.x |
| Spring Security | JWT-based |
| Spring Data JPA | Hibernate ORM |
| MySQL | 8.0+ |
| Springdoc OpenAPI | 2.3.0 |
| JUnit 5 | Unit & Integration Tests |
| Mockito | Mocking Framework |
| Maven | Build Tool |

## Prerequisites

- **Java 21** or higher
- **Maven 3.8+**
- **MySQL 8.0+**
- An IDE (IntelliJ IDEA, Eclipse, or VS Code)

## Database Setup

Run the provided SQL script to create the database and user:

```sql
-- Create the database
CREATE DATABASE IF NOT EXISTS blog_db;

-- Create the user
CREATE USER IF NOT EXISTS 'blog_user'@'localhost' IDENTIFIED BY 'blog_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON blog_db.* TO 'blog_user'@'localhost';
FLUSH PRIVILEGES;
```

Or run the script directly:

```bash
mysql -u root -p < setup-database.sql
```

## Configuration

Update `src/main/resources/application.properties` if your database credentials differ:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/blog_db
spring.datasource.username=blog_user
spring.datasource.password=blog_password

jwt.secret=your-secret-key-here
jwt.expiration=86400000
```

## Running the Application

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application starts on `http://localhost:8080`.

## API Documentation

Once the application is running, access the interactive API documentation:

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI Spec**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Using JWT Authentication in Swagger UI

1. Register a new user via `POST /api/auth/register`
2. Login via `POST /api/auth/login` — copy the `token` from the response
3. Click the **Authorize** button at the top of Swagger UI
4. Enter `<your-token>` in the `bearerAuth` field and click **Authorize**
5. All subsequent requests will include the JWT token automatically

### Including the Token in API Requests

```bash
curl -H "Authorization: Bearer <your-token>" http://localhost:8080/api/posts
```

## API Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|:---:|
| POST | `/api/auth/register` | Register a new user | No |
| POST | `/api/auth/login` | Login and get JWT token | No |
| GET | `/api/posts` | Get all posts (paginated) | No |
| GET | `/api/posts/{id}` | Get post by ID | No |
| POST | `/api/posts` | Create a new post | Yes |
| PUT | `/api/posts/{id}` | Update a post | Yes |
| DELETE | `/api/posts/{id}` | Delete a post | Yes |
| GET | `/api/posts/{postId}/comments` | Get comments for a post | No |
| GET | `/api/posts/{postId}/comments/{id}` | Get comment by ID | No |
| POST | `/api/posts/{postId}/comments` | Create a comment | Yes |
| PUT | `/api/posts/{postId}/comments/{id}` | Update a comment | Yes |
| DELETE | `/api/posts/{postId}/comments/{id}` | Delete a comment | Yes |

## Running Tests

```bash
# Run all tests
mvn test

# Run only unit tests
mvn test -Dtest="*ServiceTest,*ConfigTest"

# Run only integration tests
mvn test -Dtest="*IntegrationTest"

# Run with coverage report (requires JaCoCo plugin)
mvn test jacoco:report
```

Test reports are generated in `target/surefire-reports/`.

## Project Structure

```
src/
├── main/java/com/example/blog/
│   ├── config/          # OpenAPI configuration
│   ├── controller/      # REST controllers (Auth, Post, Comment)
│   ├── dto/             # Request/Response DTOs
│   ├── entity/          # JPA entities (User, Post, Comment, Category, Tag)
│   ├── exception/       # Global exception handling
│   ├── repository/      # Spring Data JPA repositories
│   ├── security/        # JWT authentication (filter, provider, config)
│   └── service/         # Business logic (PostService, CommentService)
└── test/java/com/example/blog/
    ├── integration/     # Integration tests (Auth, Post, Comment controllers)
    ├── service/         # Unit tests (PostService, CommentService)
    └── util/            # Test utilities (TestDataBuilder)
```

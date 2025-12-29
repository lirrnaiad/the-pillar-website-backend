# The Pillar E-Publication Website - Backend

Backend API for The Pillar E-Publication Website built with Spring Boot and REST.

## Tech Stack

- **Framework:** Spring Boot 3.2.0
- **Language:** Java 17
- **API:** REST
- **Database:** PostgreSQL
- **Build Tool:** Maven
- **Security:** Spring Security + JWT

## Project Structure

```
the-pillar-website-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/uep/pillar/
│   │   │       ├── ThePillarApplication.java      # Main Spring Boot application
│   │   │       ├── config/                        # Configuration classes
│   │   │       │   ├── SecurityConfig.java        # Security & CORS configuration
│   │   │       │   ├── CloudinaryConfig.java      # Cloudinary storage config
│   │   │       │   └── PasswordConfig.java        # Password encoder config
│   │   │       ├── controller/                    # REST controllers
│   │   │       ├── model/                         # JPA Entity models
│   │   │       │   ├── User.java
│   │   │       │   ├── Role.java
│   │   │       │   ├── Article.java
│   │   │       │   ├── Category.java
│   │   │       │   ├── Tag.java
│   │   │       │   ├── Media.java
│   │   │       │   └── PublicationIssue.java
│   │   │       ├── repository/                    # JPA Repositories
│   │   │       │   ├── UserRepository.java
│   │   │       │   ├── ArticleRepository.java
│   │   │       │   ├── CategoryRepository.java
│   │   │       │   └── ...
│   │   │       ├── service/                       # Business logic
│   │   │       │   ├── ArticleService.java
│   │   │       │   ├── UserService.java
│   │   │       │   ├── MediaService.java
│   │   │       │   └── AuthService.java
│   │   │       ├── security/                      # Security configuration
│   │   │       │   ├── JwtTokenProvider.java
│   │   │       │   ├── JwtAuthFilter.java
│   │   │       │   └── UserDetailsServiceImpl.java
│   │   │       ├── exception/                     # Exception handlers
│   │   │       │   └── GlobalExceptionHandler.java
│   │   │       ├── dto/                           # Data Transfer Objects
│   │   │       │   ├── ArticleDTO.java
│   │   │       │   └── UserDTO.java
│   │   │       └── util/                          # Utility classes
│   │   └── resources/
│   │       ├── application.properties             # Main configuration
│   │       ├── application-dev.properties         # Dev environment
│   │       └── application-prod.properties        # Production environment
│   └── test/
│       └── java/
│           └── com/uep/pillar/
│               └── ...                            # Test classes
├── pom.xml
└── README.md
```

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- IDE with Java support (IntelliJ IDEA, Eclipse, VS Code with Java extensions)

## Getting Started

### 1. Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE pillar_db;
```

Or for development:

```sql
CREATE DATABASE pillar_db_dev;
```

### 2. Configuration

Update `src/main/resources/application.properties` with your database credentials:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/pillar_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Build and Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

Or run from your IDE by executing the `ThePillarApplication` class.

### 4. Access REST API

Once running, the REST API is available at:
- **Base URL:** http://localhost:8080/api

## Development

### Running Tests

```bash
mvn test
```

### Building for Production

```bash
mvn clean package -Pprod
```

## API Endpoints

### Authentication
- `POST /api/auth/login` - Login
- `POST /api/auth/register` - Register
- `POST /api/auth/refresh` - Refresh token

### Articles
- `GET /api/articles` - List articles (with filtering, sorting, pagination)
- `GET /api/articles/{id}` - Get article by ID
- `GET /api/articles/slug/{slug}` - Get article by slug
- `POST /api/articles` - Create article
- `PUT /api/articles/{id}` - Update article
- `DELETE /api/articles/{id}` - Delete article
- `POST /api/articles/{id}/publish` - Publish article
- `POST /api/articles/{id}/archive` - Archive article
- `POST /api/articles/{id}/reject` - Reject article
- `POST /api/articles/{id}/restore` - Restore article
- `POST /api/articles/{id}/views` - Increment view count
- `PUT /api/articles/{id}/featured` - Toggle featured status
- `GET /api/articles/featured` - Get featured articles
- `GET /api/articles/recent` - Get recent articles
- `GET /api/articles/category/{categorySlug}` - Get articles by category

### Users
- `GET /api/users` - List all users
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/me` - Get current user
- `POST /api/users` - Create user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user
- `POST /api/users/{id}/change-password` - Change password

### Categories
- `GET /api/categories` - List all categories
- `GET /api/categories/{id}` - Get category by ID
- `GET /api/categories/slug/{slug}` - Get category by slug
- `POST /api/categories` - Create category
- `PUT /api/categories/{id}` - Update category
- `DELETE /api/categories/{id}` - Delete category

### Tags
- `GET /api/tags` - List all tags
- `GET /api/tags/{id}` - Get tag by ID
- `GET /api/tags/slug/{slug}` - Get tag by slug
- `POST /api/tags` - Create tag
- `PUT /api/tags/{id}` - Update tag
- `DELETE /api/tags/{id}` - Delete tag

### Media
- `GET /api/media` - List all media
- `GET /api/media/{id}` - Get media by ID
- `POST /api/media/upload` - Upload media
- `DELETE /api/media/{id}` - Delete media

### Publication Issues
- `GET /api/publication-issues` - List all issues
- `GET /api/publication-issues/{id}` - Get issue by ID
- `GET /api/publication-issues/slug/{slug}` - Get issue by slug
- `POST /api/publication-issues` - Create issue
- `PUT /api/publication-issues/{id}` - Update issue
- `DELETE /api/publication-issues/{id}` - Delete issue

### Search
- `GET /api/search/articles` - Search articles
- `GET /api/search/highlights` - Get search highlights

### Statistics
- `GET /api/statistics/articles` - Get article statistics
- `GET /api/statistics/publication` - Get publication statistics

### Article Revisions
- `GET /api/articles/{articleId}/revisions` - Get revision history
- `POST /api/articles/revisions/{revisionId}/restore` - Restore from revision

### Audit Logs
- `GET /api/audit-logs` - Get audit logs (with optional filters)

## Database Schema

Based on the SRS, the main entities are:

- **Users** - Editorial staff members
- **Roles** - User roles (Admin, Editor, Writer)
- **Articles** - Published content
- **Categories** - Content sections (News, Feature, Opinion, etc.)
- **Tags** - Article tags for search
- **Media** - Multimedia files (images, videos)
- **PublicationIssues** - Magazine issues/editions

## Security

- JWT-based authentication for admin access
- Role-based authorization
- Password encryption with BCrypt
- CORS configuration for frontend integration
- CSRF disabled for stateless REST API

### Authentication Flow

1. **Register/Login**: `POST /api/auth/register` or `POST /api/auth/login`
   - Returns JWT token in response
   - Token expires in 24 hours (configurable)

2. **Authenticated Requests**: Include token in `Authorization` header
   ```
   Authorization: Bearer <your-jwt-token>
   ```

3. **Refresh Token**: `POST /api/auth/refresh`
   - Send current token to get a new one

### CORS Configuration

CORS is configured in `SecurityConfig.java` and supports frontend integration:

**Development (default):**
- Allowed origins: `http://localhost:5173`, `http://localhost:3000`

**Production:**
Set `CORS_ALLOWED_ORIGINS` environment variable:
```bash
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
```

Or configure in `application-prod.properties`:
```properties
cors.allowed-origins=https://yourdomain.com,https://www.yourdomain.com
```

## Environment Variables

### Required for Production

| Variable | Description | Example |
|----------|-------------|---------|
| `DATABASE_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/pillar_db` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `your_password` |
| `JWT_SECRET` | Secret key for JWT tokens (use strong random string) | `your-secret-key-here` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated frontend URLs | `https://yourdomain.com,https://www.yourdomain.com` |

### Optional

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Server port | `8080` |
| `JWT_EXPIRATION` | JWT expiration in milliseconds | `86400000` (24 hours) |
| `CLOUDINARY_CLOUD_NAME` | Cloudinary cloud name | - |
| `CLOUDINARY_API_KEY` | Cloudinary API key | - |
| `CLOUDINARY_API_SECRET` | Cloudinary API secret | - |
| `CLOUDINARY_FOLDER` | Cloudinary upload folder | `pillar-uploads` |

See `.env.example` for a complete template.

## Production Deployment

### Using Docker

1. Build the Docker image:
```bash
docker build -t the-pillar-backend .
```

2. Run with environment variables:
```bash
docker run -d \
  -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://db:5432/pillar_db \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=your_password \
  -e JWT_SECRET=your-secret-key \
  -e CORS_ALLOWED_ORIGINS=https://yourdomain.com \
  -e CLOUDINARY_CLOUD_NAME=your_cloud_name \
  -e CLOUDINARY_API_KEY=your_api_key \
  -e CLOUDINARY_API_SECRET=your_api_secret \
  the-pillar-backend
```

### Using Maven

1. Build for production:
```bash
mvn clean package -Pprod
```

2. Run with production profile:
```bash
java -jar -Dspring.profiles.active=prod target/the-pillar-website-backend-1.0.0.jar
```

### Environment Setup

Create a `.env` file or set environment variables:
```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/pillar_db
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
export JWT_SECRET=your-strong-secret-key
export CORS_ALLOWED_ORIGINS=https://yourdomain.com
export CLOUDINARY_CLOUD_NAME=your_cloud_name
export CLOUDINARY_API_KEY=your_api_key
export CLOUDINARY_API_SECRET=your_api_secret
```

## API Response Formats

### Success Response
```json
{
  "id": 1,
  "title": "Article Title",
  "content": "Article content...",
  ...
}
```

### Error Response
```json
{
  "error": "Bad Request",
  "message": "Invalid input provided",
  "timestamp": "2025-12-30T01:00:00",
  "status": 400
}
```

### Paginated Response
```json
{
  "content": [...],
  "pageable": {...},
  "totalElements": 100,
  "totalPages": 10,
  "size": 10,
  "number": 0
}
```

For detailed API documentation, see [API.md](API.md).

## Team

- Sean Ivan M. Fabia
- Paolo Leandro L. Pinca
- Marl June S. Ordonia
- Khristher John B. Barrosa
- Adielyn P. Quitorio

## License

© 2025 The Pillar, University of Eastern Philippines


# The Pillar E-Publication Website - Backend

Backend API for The Pillar E-Publication Website built with Spring Boot and GraphQL.

## Tech Stack

- **Framework:** Spring Boot 3.2.0
- **Language:** Java 17
- **API:** GraphQL
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
│   │   │       │   ├── GraphQLConfig.java
│   │   │       │   ├── SecurityConfig.java
│   │   │       │   └── CorsConfig.java
│   │   │       ├── controller/                    # REST controllers (if needed)
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
│   │   │       ├── resolver/                      # GraphQL Resolvers
│   │   │       │   ├── query/                     # Query resolvers
│   │   │       │   │   ├── ArticleQueryResolver.java
│   │   │       │   │   └── UserQueryResolver.java
│   │   │       │   └── mutation/                  # Mutation resolvers
│   │   │       │       ├── ArticleMutationResolver.java
│   │   │       │       └── AuthMutationResolver.java
│   │   │       ├── security/                      # Security configuration
│   │   │       │   ├── JwtTokenProvider.java
│   │   │       │   ├── JwtAuthenticationFilter.java
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
│   │       ├── application-prod.properties        # Production environment
│   │       └── graphql/                           # GraphQL schema files
│   │           ├── schema.graphqls
│   │           └── ...
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

### 4. Access GraphQL Playground

Once running, access the GraphQL Playground at:
- **GraphQL Endpoint:** http://localhost:8080/graphql
- **GraphQL Playground:** http://localhost:8080/playground

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

- **GraphQL:** `/graphql` (POST)
- **GraphQL Playground:** `/playground` (GET) - Development only

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
- Password encryption
- CORS configuration for frontend integration

## Environment Variables (Production)

- `DATABASE_URL` - PostgreSQL connection URL
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password
- `JWT_SECRET` - Secret key for JWT tokens

## Team

- Sean Ivan M. Fabia
- Paolo Leandro L. Pinca
- Marl June S. Ordonia
- Khristher John B. Barrosa
- Adielyn P. Quitorio

## License

© 2025 The Pillar, University of Eastern Philippines


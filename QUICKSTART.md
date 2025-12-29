# Quick Start Guide

## Running the Backend

### Option 1: Using the Run Script (Recommended)

The `run.sh` script automatically:
- Loads environment variables from `.env` file
- Sets Java 17
- Starts the Spring Boot application

```bash
cd the-pillar-website-backend
./run.sh
```

### Option 2: Using the Build Script

```bash
cd the-pillar-website-backend
./build.sh spring-boot:run
```

### Option 3: Manual Setup

If you prefer to run manually:

```bash
cd the-pillar-website-backend

# Load .env file
set -a
source .env
set +a

# Set Java 17
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk

# Run
mvn spring-boot:run
```

## Verifying Cloudinary Integration

Once the application is running, you can verify Cloudinary is configured:

1. **Check the logs** - Look for Cloudinary initialization messages
2. **Test the upload endpoint** - The `/api/media/upload` endpoint should be available
3. **Check GraphQL** - Visit http://localhost:8080/altair to test GraphQL queries

## Environment Variables

The `.env` file should contain:

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/pillar_db
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password

# Cloudinary (required for media uploads)
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret
```

## Troubleshooting

### Cloudinary Not Working

If Cloudinary isn't initializing:
1. Check that `.env` file exists and has correct credentials
2. Verify you're using `./run.sh` or `./build.sh` (they load `.env` automatically)
3. Check application logs for error messages

### Application Won't Start

1. Ensure Java 17 is installed: `java -version`
2. Check database is running and accessible
3. Verify `.env` file has correct database credentials


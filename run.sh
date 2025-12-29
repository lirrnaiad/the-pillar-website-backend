#!/bin/bash
# Simple run script that loads .env and starts the application

cd "$(dirname "$0")"

# Load .env file if it exists
if [ -f .env ]; then
    echo "Loading environment variables from .env file..."
    set -a  # automatically export all variables
    source .env
    set +a  # stop automatically exporting
    echo "✓ Environment variables loaded"
    echo ""
fi

# Use Java 17
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export PATH=$JAVA_HOME/bin:$PATH

echo "Starting Spring Boot application..."
echo "Java version: $(java -version 2>&1 | head -1)"
echo ""

mvn spring-boot:run

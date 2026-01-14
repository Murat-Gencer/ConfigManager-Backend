#!/bin/bash

# ConfigManager Backend Startup Script
echo "🚀 Starting ConfigManager Backend..."

# Navigate to backend directory
cd "$(dirname "$0")/backend"

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or higher."
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{print $1}')
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java 17 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

echo "✅ Java version check passed"

# Check if Maven wrapper exists
if [ ! -f "./mvnw" ]; then    echo "❌ Maven wrapper not found. Please ensure mvnw exists in the backend directory."
    exit 1
fi

# Make Maven wrapper executable
chmod +x ./mvnw

echo "📦 Starting Spring Boot application..."
echo "🌐 Backend will be available at: http://localhost:8080"
echo "🗄️  H2 Console will be available at: http://localhost:8080/h2-console"
echo ""
echo "Press Ctrl+C to stop the backend server"
echo "----------------------------------------"

# Start the Spring Boot application
./mvnw spring-boot:run

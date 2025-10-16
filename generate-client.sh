#!/bin/bash

# Script to generate TypeScript React client from OpenAPI spec
# This script starts the Spring Boot application, downloads the OpenAPI spec,
# and generates the TypeScript client code.

set -e

echo "🚀 Starting client generation process..."

# Check if the application is already running
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null ; then
    echo "✅ Application is already running on port 8080"
    APP_WAS_RUNNING=true
else
    echo "📦 Starting Spring Boot application..."
    mvn spring-boot:run > /dev/null 2>&1 &
    APP_PID=$!
    APP_WAS_RUNNING=false

    # Wait for application to be ready
    echo "⏳ Waiting for application to start..."
    for i in {1..60}; do
        if curl -s http://localhost:8080/v3/api-docs > /dev/null 2>&1; then
            echo "✅ Application is ready!"
            break
        fi
        if [ $i -eq 60 ]; then
            echo "❌ Application failed to start within 60 seconds"
            exit 1
        fi
        sleep 1
    done
fi

# Download OpenAPI spec
echo "📥 Downloading OpenAPI specification..."
curl -s http://localhost:8080/v3/api-docs -o openapi.json

if [ ! -f openapi.json ]; then
    echo "❌ Failed to download OpenAPI spec"
    exit 1
fi

echo "✅ OpenAPI spec downloaded successfully"

# Generate TypeScript client
echo "🔨 Generating TypeScript React client..."
mvn openapi-generator:generate@generate-typescript-react-client

if [ $? -eq 0 ]; then
    echo "✅ Client generated successfully in ./generated-client"
else
    echo "❌ Failed to generate client"
    exit 1
fi

# Stop the application if we started it
if [ "$APP_WAS_RUNNING" = false ]; then
    echo "🛑 Stopping Spring Boot application..."
    kill $APP_PID 2>/dev/null || true
fi

echo "🎉 Client generation complete!"
echo ""
echo "📁 Generated files are in: ./generated-client"
echo "📖 To use the client in your React app:"
echo "   1. Copy the generated-client folder to your React project"
echo "   2. Install dependencies: npm install"
echo "   3. Import and use the generated API client"


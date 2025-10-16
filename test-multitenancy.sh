# Build the JAR
./mvnw clean package -DskipTests

# Start everything
docker compose up --build

# Wait for startup, then provision tenants and test
curl http://localhost:8080/api/tenants
curl -X POST http://localhost:8080/api/tenants/acme/provision

# Verify tenant databases were created
docker exec -it mas-java-poc-postgres-1 psql -U myuser -d postgres -c "\l"

# Use a lightweight OpenJDK image
FROM eclipse-temurin:25-jre-alpine

# Set working directory
WORKDIR /app

# Copy the built jar (assume it's built as target/mas-java-poc-0.0.1-SNAPSHOT.jar)
COPY target/mas-java-poc-0.0.1-SNAPSHOT.jar app.jar

# Copy wait-for-it script
COPY wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

# Expose port 8080
EXPOSE 8080

# Set environment variables for catalog DB connection (can be overridden by compose)
ENV SPRING_DATASOURCE_CATALOG_URL=jdbc:postgresql://catalog-postgres:5432/catalog_db
ENV SPRING_DATASOURCE_CATALOG_USERNAME=myuser
ENV SPRING_DATASOURCE_CATALOG_PASSWORD=secret

# Wait for catalog DB, then run the app
ENTRYPOINT ["/wait-for-it.sh", "catalog-postgres:5432", "--", "java", "-jar", "app.jar"]

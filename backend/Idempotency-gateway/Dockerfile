# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the built jar file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Render assigns a dynamic PORT via environment variable.
# We set a default of 8081, but it can be overridden.
ENV PORT=8081
EXPOSE $PORT

# Run the jar file, dynamically binding to the PORT environment variable
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT}"]

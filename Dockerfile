# # Use official Java 21 image
# FROM openjdk:21-jdk-slim

# # Set working directory
# WORKDIR /app

# # Copy Maven wrapper and pom.xml
# COPY mvnw .
# COPY .mvn .mvn
# COPY pom.xml .

# # Download dependencies (cache layer)
# RUN ./mvnw dependency:go-offline -B

# # Copy project source code
# COPY src ./src

# # Build the application
# RUN ./mvnw clean package -DskipTests

# # Copy the built jar file
# COPY target/*.jar app.jar

# # Expose port (Render dynamically assigns one)
# ENV PORT=8080
# EXPOSE 8080

# # Run the app
# CMD ["java", "-jar", "app.jar"]


# Use stable Java 21 image
FROM eclipse-temurin:21-jdk-jammy

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# ✅ Make mvnw executable
RUN chmod +x mvnw

# Download dependencies (cache layer)
RUN ./mvnw dependency:go-offline -B

# Copy project source
COPY src ./src

# Build the app
RUN ./mvnw clean package -DskipTests

# Copy final jar to root
RUN cp target/*.jar app.jar

# Expose dynamic port (for Render or Docker)
ENV PORT=8080
EXPOSE 8080

# Run the app
CMD ["java", "-jar", "app.jar"]

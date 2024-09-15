# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-alpine

# Set the working directory in the container
WORKDIR /app

# Copy the executable JAR file to the working directory
COPY build/libs/pre-signed-url-service-0.0.1-SNAPSHOT.jar /app/pre-signed-url-app.jar

# Expose the port that your Spring Boot app runs on
EXPOSE 8081

# Command to run the JAR file
ENTRYPOINT ["java", "-jar", "/app/pre-signed-url-app.jar"]

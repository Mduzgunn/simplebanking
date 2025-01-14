# Build stage
FROM gradle:7.6.1-jdk11 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar -x test --no-daemon

# Run stage
FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"] 
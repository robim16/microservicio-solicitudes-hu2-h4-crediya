# Stage 1: Build the application
FROM gradle:8.5-jdk21 AS build
WORKDIR /app
COPY --chown=gradle:gradle . /app
RUN gradle bootJar --no-daemon

# Stage 2: Create the final runtime image
FROM bellsoft/liberica-runtime-container:jre-21-musl
WORKDIR /app
EXPOSE 8082
COPY --from=build /app/build/libs/*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
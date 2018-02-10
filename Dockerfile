# Based on Alpine linux with OpenJDK 7
FROM openjdk:7-jdk-alpine

# Mount /tmp directory for embedded Tomcat
VOLUME /tmp

# Build argument to pass JAR file
ARG JAR_FILE

# Copy uber-jar using new short name
COPY ${JAR_FILE} app.jar

# Run application passing JVM option to speedup Tomcat init
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]

# Default parameters
CMD ["--filesystem.base=/"]


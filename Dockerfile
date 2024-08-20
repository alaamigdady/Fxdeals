FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/fxdeals-0.0.1-SNAPSHOT.jar fxdeals.jar
ENTRYPOINT ["java", "-jar", "fxdeals.jar"]
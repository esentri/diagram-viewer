FROM amazoncorretto:17-alpine

WORKDIR /app

# Kopiere die JAR-Datei in den Container
# Ersetze 'deine-app.jar' durch den echten Namen aus target/
COPY build/libs/diagram-viewer-0.1.jar app.jar

# Port für Spring Boot / Vaadin
EXPOSE 8090

# Startbefehl
ENTRYPOINT ["java", "-jar", "app.jar"]

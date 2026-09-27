FROM amazoncorretto:17-alpine

WORKDIR /app

# Kopiere die JAR-Datei in den Container
# Ersetze 'deine-app.jar' durch den echten Namen aus target/
COPY build/libs/diagram-viewer-0.2.jar app.jar

# Port für Spring Boot / Vaadin
EXPOSE 8090

# Heap, siehe README "Memory"; beim Start überschreibbar per -e JAVA_TOOL_OPTIONS=...
ENV JAVA_TOOL_OPTIONS="-Xmx2g -XX:+UseG1GC -XX:+UseStringDeduplication"

# Startbefehl
ENTRYPOINT ["java", "-jar", "app.jar"]

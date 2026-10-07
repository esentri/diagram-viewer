FROM amazoncorretto:21-alpine

RUN apk add --no-cache fontconfig freetype font-liberation ttf-dejavu

RUN printf '%s\n' \
  '<?xml version="1.0"?>' \
  '<!DOCTYPE fontconfig SYSTEM "fonts.dtd">' \
  '<fontconfig>' \
  '  <alias><family>Helvetica</family><prefer><family>Liberation Sans</family></prefer></alias>' \
  '  <alias><family>helvetica</family><prefer><family>Liberation Sans</family></prefer></alias>' \
  '  <alias><family>Arial</family><prefer><family>Liberation Sans</family></prefer></alias>' \
  '  <alias><family>sans-serif</family><prefer><family>Liberation Sans</family></prefer></alias>' \
  '  <alias><family>monospace</family><prefer><family>DejaVu Sans Mono</family></prefer></alias>' \
  '</fontconfig>' > /etc/fonts/local.conf && \
  fc-cache -f

WORKDIR /app

# Kopiere das ausführbare JAR in den Container (unabhängig von der Version, ohne das -plain.jar)
COPY build/libs/diagram-viewer-*[0-9].jar app.jar

# Port für Spring Boot / Vaadin
EXPOSE 8090

# Heap, siehe README "Memory"; beim Start überschreibbar per -e JAVA_TOOL_OPTIONS=...
ENV JAVA_TOOL_OPTIONS="-Xmx2g -XX:+UseG1GC -XX:+UseStringDeduplication"

# Startbefehl
ENTRYPOINT ["java", "-jar", "app.jar"]

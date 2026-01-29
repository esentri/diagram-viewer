# DLC Diagram Viewer

### What it does
The Diagram Viewer is a service to create and manage UML class diagrams generated with the DLC diagram generator 
plugin. It helps you analyze Domain‑Driven Design (DDD) projects built with DLC or JMolecules by browsing, rendering, 
and inspecting your domain model diagrams.\
Furthermore, you can grant project access to various users in the UI. Users can register and log in either via the 
Diagram Viewer's own user management or via an external identity provider (Okta).\
New projects (Domain-Mirror models) can also be uploaded via the API. For this to work, you need to obtain an API-Token
in the UI.

Have a look at the [Confluence Plugin](https://bitbucket.org/esentri/diagram-viewer-forge-app/) built for the Diagram 
Viewer. It allows you to stream your diagrams in real-time to Confluence for smooth development and always keeping your 
documentation up to date.

For maximum efficiency, you can use the Diagram Viewer plugins for 
[Maven](https://github.com/esentri/domainlifecycles/tree/main/dlc-maven-plugin) or 
[Gradle](https://github.com/esentri/domainlifecycles/tree/main/dlc-gradle-plugin).\
They allow you to automatically upload your recent Domain-Mirror model to the Diagram Viewer, so you don't have to 
consistently update them yourself while developing.

#### Prerequisites
- Java 17+
- Docker and Docker Compose
- Gradle (wrapper included)

## Quickstart (local development)
1) Start required services (Kroki and PostgreSQL)
```
docker compose -f docker/dev/docker-compose.yaml up -d
```
Starts:
- Kroki (NomNoml conversion) at http://localhost:8000
- PostgreSQL at localhost:5432 with user=user, password=password, db=diagram-viewer-db

2) Run the service
```
./gradlew bootRun
```

4) Open the UI
http://localhost:8090

### Minimal configuration (only if needed)
Defaults are optimized for local use and will work out of the box.
Override via environment variables:
- SERVER_PORT: HTTP port (default 8090)
- DIAGRAMS_LOCATION: path to diagram sources (default `diagrams`)
- KROKI_CONTAINER_URL: Kroki endpoint (default `http://localhost:8000`)
- Database: `RDS_HOSTNAME` (localhost), `RDS_PORT` (5432), `RDS_DB_NAME` (diagram-viewer-db), `RDS_USERNAME` (user), `RDS_PASSWORD` (password)
- Authentication (Okta): `OAUTH_ISSUER`, `OAUTH_CLIENT_ID` — set these if your environment requires login

### Ports
- 8090 — Diagram Viewer (UI/API)
- 8000 — Kroki
- 5432 — PostgreSQL

### Health check
```
GET http://localhost:8090/actuator/health
```
# DLC Diagram Viewer

### What it does
The Diagram Viewer is a service to create and share Domain-Driven Design (DDD) specific UML class diagrams.
Diagrams are created from a DDD model derived from the domain model implementation, that uses [DLC](https://github.com/esentri/domainlifecycles) marker interfaces. 

The [DLC build plugin](https://github.com/esentri/domainlifecycles/tree/main/dlc-plugins) (Maven or Gradle is supported) can be used to
generate this model by analyzing DLC marker interfaces that represent the relevant tactical DDD building blocks.
The Diagram Viewer helps you analyze Java DDD projects built with DLC by browsing, rendering, and inspecting your domain model diagrams.

If the implementation changes and a new model version is pushed to the Diagram Viewer, the corresponding diagrams are updated automatically.
That helps to reduce the so-called model-code gap and keeps your documentation always up to date.
Furthermore, you can share your diagrams with other team members and add notes to explain design decisions or trade offs.

### Usage
Check our [User Guide](./USER_GUIDE.md) for more information.

### Build & Deploy
#### Prerequisites

Running the service:
- Docker and Docker Compose

For development:
- Java 17+
- Gradle (wrapper included)

The diagram viewer depends on a inner plugin lib. 
It must be cloned and built first for a local build of the diagram viewer:

- git clone https://github.com/esentri/diagram-viewer-plugin.git
- cd diagram-viewer-plugin
- ./gradlew build publishToMavenLocal

Then clone this repository and build the diagram viewer:
- git clone https://github.com/esentri/diagram-viewer.git
- cd diagram-viewer
- ./gradlew build


#### Quickstart 

##### Run application with Docker
1) Start containers (Kroki, PostgreSQL and DiagramViewer)
```
docker compose -f docker/run/docker-compose.yaml up -d
```
2) Open the UI
   http://localhost:8090

##### Build from source (run for development)
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

#### Configuration options
Defaults are optimized for local use and will work out of the box.

Override via environment variables:
- `SERVER_PORT`: HTTP port (default 8090)
- `DIAGRAMS_LOCATION`: path to diagram sources on server (default `diagrams`)
- `KROKI_CONTAINER_URL`: Kroki endpoint (default `http://localhost:8000`)
- Database: 
  - `DB_HOSTNAME` (localhost)
  - `DB_PORT` (5432)
  - `DB_NAME` (diagram-viewer-db)
  - `DB_USERNAME` (user)
  - `DB_PASSWORD` (password)
- Manual Jar Upload (disabled by default): `JAR_UPLOAD_ENABLED` (true/false) 
- Additional Okta Authentication (disabled by default):
  - `OKTA_LOGIN_ENABLED` (true/false)
  If enabled, you need to set Spring Okta starter environment variables:
  - `OKTA_OAUTH2_ISSUER`
  - `OKTA_OAUTH2_CLIENT_ID`
  - `OKTA_OAUTH2_REDIRECT_URI`
- `REGENERATE_DIAGRAMS_TASK_RATE`: Digrams are regenerated, if a new Domain Model was pushed to the Diagram Viewer 
   (default: check for new domain model version every 30 sec)

#### Ports
- 8090 — Diagram Viewer (UI/API)
- 8000 — Kroki
- 5432 — PostgreSQL

#### Health check
```
GET http://localhost:8090/actuator/health
```


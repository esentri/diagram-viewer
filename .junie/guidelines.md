# DLC Diagram Viewer Development Guidelines
## Build/Configuration Instructions

### Prerequisites
- Java 17 or higher
- Docker and Docker Compose
- Gradle

### Setup and Running
1. Start the required Docker containers:
   ```bash
   docker-compose up -d
   ```

2. Build and run the application:
   ```bash
   ./gradlew bootRun
   ```

3. Access the application at http://localhost:8090

# Task Management Application

Task management platform built with a Spring Boot backend and a React frontend. The application is designed to feel closer to a modern work-management product than a simple CRUD panel, with role-based access control, project coordination, task planning, collaboration, and operational visibility.

## Product Highlights

- Role-aware workspace for `ADMIN`, `PROJECT_MANAGER`, `TEAM_LEADER`, and `TEAM_MEMBER`
- Kanban-style task execution with richer planning signals
- Task scheduling with `startDate` and `dueDate`
- Executive overview with delivery risk and timeline visibility
- Project health snapshots with completion progress
- File uploads and comment threads attached directly to work items
- Optional RabbitMQ domain events and Redis-backed shared caching
- Dockerized full-stack setup for local demos and portfolio presentation

## Core Capabilities

- Authentication and token-based session flow
- Project and task lifecycle management
- Task command center with search, filters, risk focus, and roadmap timeline
- Executive dashboard with overdue work, due-this-week visibility, and delivery pulse
- Attachments and comments tied to tasks
- Public system entry endpoints for friendlier first-run and deployment checks
- GitHub Actions workflows for CI and container publishing

## Stack

- Java 21
- Spring Boot 3.5.12
- Spring Security
- Spring Data JPA
- MySQL
- H2 for tests
- RabbitMQ for optional domain events
- Redis for optional shared caching
- React 18
- Vite 5

## Project Structure

```text
Task-Management-Application/
|-- backend/
|   |-- pom.xml
|   |-- src/
|   |   |-- main/
|   |   |   |-- java/io/github/barisaltinel/taskmanagement
|   |   |   `-- resources/
|   |   `-- test/
|   |       `-- java/io/github/barisaltinel/taskmanagement
|-- frontend/
|   |-- package.json
|   `-- src/
`-- README.md
```

Backend package root: `io.github.barisaltinel.taskmanagement`

## Recent Enhancements

The current version includes a more productized planning layer inspired by tools such as Jira and monday.com:

- tasks now support `startDate` and `dueDate`
- backend validates delivery schedules before saving
- frontend shows `Delivery Timeline`, `Roadmap Timeline`, overdue work, and upcoming deadlines
- task creation now uses richer planning fields instead of a minimal form-only flow
- overview pages surface team load, project health, and delivery risks for demo-ready storytelling

## Security Notes

- Production secrets are not stored in the repository.
- `backend/src/main/resources/application.properties` now expects database credentials from environment variables.
- Use `backend/src/main/resources/application.example.properties` as a local setup template.
- The optional bootstrap admin user is only created when `APP_BOOTSTRAP_ADMIN_EMAIL` and `APP_BOOTSTRAP_ADMIN_PASSWORD` are provided.

## Backend Setup

### Prerequisites

- Java 21
- Maven
- MySQL running locally or remotely
- RabbitMQ only if you plan to enable event publishing
- Redis only if you plan to enable shared caching

### Configuration

Set these environment variables before starting the backend:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/taskmanagement?useSSL=true&requireSSL=true&serverTimezone=UTC"
$env:SPRING_DATASOURCE_USERNAME="your_mysql_username"
$env:SPRING_DATASOURCE_PASSWORD="your_mysql_password"
```

Optional bootstrap admin variables:

```powershell
$env:APP_BOOTSTRAP_ADMIN_NAME="Bootstrap Admin"
$env:APP_BOOTSTRAP_ADMIN_EMAIL="admin@example.com"
$env:APP_BOOTSTRAP_ADMIN_PASSWORD="change-this-password"
```

If you prefer a file-based local setup, use `backend/src/main/resources/application.example.properties` as your template and keep real secrets out of version control.

### Optional RabbitMQ and Redis

RabbitMQ and Redis are both opt-in. The example backend config keeps them disabled by default:

```properties
app.rabbitmq.enabled=false
app.redis.enabled=false
```

That way, a normal local or production startup does not depend on either service unless you explicitly turn one on.

Enable RabbitMQ when you want the backend to publish task-management events through a broker:

```properties
app.rabbitmq.enabled=true
app.rabbitmq.exchange=taskmanagement.events
app.rabbitmq.queue=taskmanagement.activity
app.rabbitmq.routing-key=taskmanagement.activity
spring.rabbitmq.host=your-rabbitmq-host
spring.rabbitmq.port=5672
spring.rabbitmq.username=your-rabbitmq-user
spring.rabbitmq.password=your-rabbitmq-password
spring.rabbitmq.virtual-host=/
```

Use durable exchange and queue names in shared environments, and keep broker credentials in environment variables or your secret manager rather than committing them.

Enable Redis when you want a shared cache instead of the backend's in-memory fallback cache:

```properties
app.redis.enabled=true
app.redis.cache-ttl=10m
spring.data.redis.host=your-redis-host
spring.data.redis.port=6379
spring.data.redis.password=your-redis-password
spring.data.redis.database=0
spring.data.redis.timeout=2s
```

For production, point Redis at a managed instance or a secured private deployment, keep a realistic TTL for your workload, and avoid exposing it publicly without authentication and network controls.

### Run Backend

```powershell
cd backend
mvn spring-boot:run
```

Backend default URL:

- `http://localhost:8080`

## Frontend Setup

```powershell
cd frontend
npm install
npm run dev
```

Frontend default URL:

- `http://localhost:5173`

## Demo Experience

For the best walkthrough, use the application in this order:

1. Sign in with a role-based account.
2. Open the overview screen to review delivery pulse, risk watch, and project health.
3. Move to the task command center to filter work by assignee, priority, and deadline status.
4. Use scheduled tasks to demonstrate roadmap and due-date visibility.
5. Attach files and comments to show collaboration around execution.

## Test and Build

Backend tests:

```powershell
cd backend
mvn test
```

Test configuration in `backend/src/test/resources/application-test.properties` explicitly keeps `app.rabbitmq.enabled=false` and `app.redis.enabled=false`, so the suite stays independent from local RabbitMQ or Redis services.

Frontend production build:

```powershell
cd frontend
npm run build
```

Backend packaging:

```powershell
cd backend
mvn -DskipTests package
```

## CI/CD

GitHub Actions is wired for a free-account-friendly baseline:

- `/.github/workflows/ci.yml` runs backend tests, frontend build, and `docker compose config`
- `/.github/workflows/docker-publish.yml` builds and publishes backend and frontend container images to GitHub Container Registry

The CI workflow runs on pushes to `main`, pull requests targeting `main`, and manual dispatches.

The frontend does not have an automated test script yet, so the workflow validates that side by running a production build.

The container publish workflow runs when you push a version tag such as `v1.0.0`, and it can also be triggered manually from the Actions tab.

Example release flow:

```powershell
git tag v1.0.0
git push origin v1.0.0
```

Published image names:

- `ghcr.io/<your-github-username>/taskmanagement-backend`
- `ghcr.io/<your-github-username>/taskmanagement-frontend`

For image publishing to GitHub Container Registry, the workflow uses the repository `GITHUB_TOKEN`, so no extra registry password is required for the same GitHub owner. If you later add real deployment to a VPS, cloud VM, or platform service, you will still need environment-specific secrets there.

The usernames and passwords inside `docker-compose.yml` are only for local development. Do not reuse those defaults as production or deployment secrets.
## Code Style

Backend formatting uses Spotless with Google Java Format:

```powershell
cd backend
./mvnw spotless:apply
```

Frontend formatting uses Prettier:

```powershell
cd frontend
npm run format
```

## Docker Setup

Run the full stack with Docker Compose:

```powershell
docker compose up --build
```

If one of the default host ports is already in use on your machine, you can override it at startup:

```powershell
$env:MYSQL_PORT="3307"
$env:BACKEND_PORT="18080"
$env:FRONTEND_PORT="13000"
docker compose up --build
```

Example alternate host ports used successfully for this project:

```powershell
$env:MYSQL_PORT="3307"
$env:RABBITMQ_PORT="5673"
$env:RABBITMQ_MANAGEMENT_PORT="15673"
$env:REDIS_PORT="6380"
$env:BACKEND_PORT="18080"
$env:FRONTEND_PORT="13000"
docker compose up --build
```

This starts:

- `mysql` on `localhost:3306`
- `rabbitmq` on `localhost:5672`
- RabbitMQ management UI on `http://localhost:15672`
- `redis` on `localhost:6379`
- backend API on `http://localhost:8080`
- frontend UI on `http://localhost:3000`

With the alternate host-port example above, the URLs become:

- frontend UI on `http://localhost:13000`
- backend API on `http://localhost:18080`
- RabbitMQ management UI on `http://localhost:15673`

The compose stack wires the containers together like this:

- frontend serves the built React app with Nginx
- Nginx proxies `/api` requests to the backend container
- backend connects to MySQL, RabbitMQ, and Redis through the internal Docker network
- uploaded files are stored in the named volume `backend_uploads`

In the Docker stack, RabbitMQ and Redis are enabled on purpose so the backend can exercise the messaging and shared-cache flows end to end.

Container files added for this flow:

- `docker-compose.yml`
- `backend/Dockerfile`
- `frontend/Dockerfile`

If you want different credentials or bootstrap admin values, adjust the environment block in `docker-compose.yml` before starting the stack.

The usernames and passwords inside `docker-compose.yml` are for local development only. Do not reuse them in production.

## Main API Areas

- `/api/auth`
- `/api/projects`
- `/api/tasks`
- `/api/attachments`
- `/api/users`
- `/api/comments`

## Roles

- `ADMIN`
- `PROJECT_MANAGER`
- `TEAM_LEADER`
- `TEAM_MEMBER`

## Positioning

This repository now presents well as:

- a portfolio-grade full-stack task management product
- a bootcamp capstone evolved into a more production-minded workspace
- a base for adding deeper product-management features such as task dependencies, automations, forms, and audit trails


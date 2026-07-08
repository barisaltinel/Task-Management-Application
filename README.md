# Task Management Application

Task management platform with a Spring Boot backend and a React frontend. The project supports role-based access control, project and task workflows, attachments, comments, and a Kanban-style task board.

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

This starts:

- `mysql` on `localhost:3306`
- `rabbitmq` on `localhost:5672`
- RabbitMQ management UI on `http://localhost:15672`
- `redis` on `localhost:6379`
- backend API on `http://localhost:8080`
- frontend UI on `http://localhost:3000`

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

# Task Management Application

Task management platform with a Spring Boot backend and a React frontend. The project supports role-based access control, project and task workflows, attachments, comments, and a Kanban-style task board.

## Stack

- Java 21
- Spring Boot 3.5.12
- Spring Security
- Spring Data JPA
- MySQL
- H2 for tests
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

Frontend production build:

```powershell
cd frontend
npm run build
```

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



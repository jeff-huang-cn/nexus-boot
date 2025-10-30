# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

nexus-boot is a Java Spring Boot 3.5.6 framework with a React admin UI. The project uses Java 17 and consists of two main modules:

- **nexus-backend-admin**: Spring Boot backend application with embedded React admin UI
- **nexus-framework**: Core framework module providing reusable utilities and infrastructure

## Build & Run Commands

### Backend (Java/Maven)

```bash
# Build entire project
mvn clean install

# Build specific module
cd nexus-backend-admin && mvn clean install
cd nexus-framework && mvn clean install

# Run backend application
cd nexus-backend-admin && mvn spring-boot:run

# Run tests
mvn test
```

The backend runs on port 8081 by default.

### Frontend (React UI)

```bash
# Navigate to UI directory
cd nexus-backend-admin/ui

# Install dependencies
npm install

# Start development server
npm start

# Build for production
npm build

# Run tests
npm test
```

The frontend development server runs on port 3000 by default.

## Architecture

### Module Structure

- **nexus-boot** (parent): Maven parent POM managing dependencies and versions
  - **nexus-backend-admin**: Backend application module
    - Java backend code in `src/main/java/com/nexus/backend/admin/`
    - React UI code in `ui/src/`
    - Application entry: `BackendAdminApplication.java`
  - **nexus-framework**: Reusable framework module
    - Framework code in `src/main/java/com/nexus/framework/`

### Backend Architecture

The backend follows a layered architecture:

- **Controller Layer** (`controller/`): REST API endpoints organized by domain (auth, codegen, permission, user, tenant, dict)
  - Each controller has a `vo/` subdirectory for request/response objects
- **Service Layer** (`service/`): Business logic organized by domain with `impl/` subdirectories
- **Data Access Layer** (`dal/`):
  - `dataobject/`: Entity classes (DO suffix)
  - `mapper/`: MyBatis-Plus mapper interfaces

Domain modules include:
- `auth`: Authentication and authorization
- `codegen`: Code generation from database tables
- `permission`: Menu and role management
- `user`: User management
- `tenant`: Multi-tenancy support
- `dict`: Dictionary/configuration data

### Framework Module (nexus-framework)

Provides infrastructure components:

- **mybatis**: MyBatis-Plus configuration and base entities
  - `BaseDO`, `BaseCreateDO`, `BaseUpdateDO`: Base entity classes with common fields
  - `BasePageQuery`: Pagination support
  - `DefaultDBFieldHandler`: Automatic field population (create/update timestamps, user IDs)
- **security**: JWT-based authentication with JWK rotation
  - Uses Spring Security OAuth2 Resource Server
  - JWT signing/verification with database-stored JWKs
  - Token blacklist via Redis
  - Custom filters, handlers, and authentication logic
- **tenant**: Multi-tenancy context and utilities
- **excel**: Excel import/export utilities using EasyExcel
- **web**: Global exception handling and unified response format
- **utils**: Common utilities (collection, date, object, tree structures)
- **config**: Redis and other infrastructure configuration

### Frontend Architecture

React + TypeScript application using:

- **React 19** with React Router 7
- **Ant Design 5** for UI components
- **Axios** for HTTP requests

Directory structure:
- `components/`: Reusable UI components
- `pages/`: Page components (Login, Dashboard, Profile, Settings, dev, system)
- `services/`: API service modules (authApi, codegen, datasources, menu, role, tenant, user)
- `router/`: Route configuration (`AppRoutes.tsx`, `DynamicRoutes.tsx`)
- `contexts/`: React contexts
- `hooks/`: Custom React hooks
- `utils/`: Utility functions

### Key Technologies

**Backend:**
- Spring Boot 3.5.6 (Java 17)
- MyBatis-Plus 3.5.12 with Join extension
- Spring Security with OAuth2 Resource Server
- MySQL 9.3.0 with HikariCP
- Dynamic datasource support
- Redis with Redisson
- Lombok, MapStruct for code generation
- Hutool, Guava for utilities
- EasyExcel for Excel operations
- Velocity for templating

**Frontend:**
- React 19 with TypeScript 4.9
- Ant Design 5
- React Router 7
- Axios for API calls
- React Scripts 5

### Security Model

JWT-based authentication with JWK rotation:
- Private keys stored in database, used for signing tokens
- Public keys cached in Redis for verification
- Automatic key rotation with configurable validity periods
- Token blacklist in Redis for logout
- Method-level security with `@PreAuthorize`
- Dynamic permission loading from database

Configuration in `application.yaml`:
```yaml
nexus:
  security:
    jwk:
      validity-days: 90
      rotation-advance-days: 7
      cache-expire-hours: 24
      auto-rotation-enabled: true
```

### Database Configuration

Uses dynamic datasource with master/slave support:
- Primary datasource: `master`
- Connection pool: HikariCP
- ORM: MyBatis-Plus with automatic CRUD operations
- Logical delete support (deleted field: 0=active, 1=deleted)
- Underscore to camel case mapping

### Component Scanning

The application scans:
- `com.nexus.backend.admin.*` (backend-admin module)
- `com.nexus.framework.*` (framework module)

MyBatis mappers scanned from: `com.nexus.**.dal.mapper`

## Development Notes

### Code Generation

The codegen module supports:
- Database table introspection
- Multiple datasource management
- Template-based code generation using Velocity
- Generate controller, service, mapper, and entity classes from database tables

### Annotation Processors

Maven compiler is configured with annotation processor paths for:
- Lombok (must be processed first)
- MapStruct (for DTO mapping)

Both are required for compilation to work correctly.

### Maven Repositories

Uses Chinese mirror repositories for faster dependency resolution:
- Huawei Cloud: https://mirrors.huaweicloud.com/repository/maven/
- Aliyun: https://maven.aliyun.com/repository/public
- Spring milestones and snapshots

### Testing

Backend tests use Spring Boot Test with Security Test support. Frontend tests use React Testing Library with Jest.

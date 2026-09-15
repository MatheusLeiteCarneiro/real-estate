# Real Estate

![Java](https://img.shields.io/badge/Java-25-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![OAuth2](https://img.shields.io/badge/OAuth2-PKCE-000000?style=for-the-badge)
![Flyway](https://img.shields.io/badge/Flyway-Migrations-CC0200?style=for-the-badge&logo=flyway&logoColor=white)

[![CI](https://img.shields.io/github/actions/workflow/status/MatheusLeiteCarneiro/real-estate/ci.yml?branch=main&style=for-the-badge&logo=github&label=CI)](https://github.com/MatheusLeiteCarneiro/real-estate/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](LICENSE)

A REST API for a real estate platform built with Spring Boot, PostgreSQL, OAuth2 Authorization Code with PKCE, JWT, Cloudinary, Flyway, Swagger, Docker Compose, and Postman.

The project started as a way to practice Java backend development with a more realistic structure than a simple CRUD. It includes authentication, role-based access, broker ownership rules, validation, file upload handling, database migrations, API documentation, and environment-based configuration.

## Project Status

- Backend API intended for portfolio use, with security, maintainability, and practical business rules in mind.
- Runs locally with Docker Compose, PostgreSQL, Swagger UI, and Postman OAuth2 testing.
- Designed as a backend foundation for a future real estate marketplace frontend.

## Highlights

- OAuth2 Authorization Server with Authorization Code + PKCE
- JWT access tokens with custom `authorities` and `username` claims
- Resource Server protection for authenticated endpoints
- Role hierarchy where `ADMIN` includes `BROKER` permissions
- Broker ownership validation for property and image mutations
- Admin-only user management
- Public property catalog with filtering, sorting, pagination, and primary images
- Cloudinary integration for property image uploads and deletion
- Transactional rollback behavior for failed image uploads
- Flyway-managed PostgreSQL schema evolution
- Swagger UI configured with OAuth2 PKCE
- Postman collection and environment ready for token-based testing
- Docker Compose setup with PostgreSQL health checks
- Automated tests covering validation, mapping, service behavior, ownership rules, and basic security access checks
## Tech Stack

| Area         | Technology                                                                      |
|--------------|---------------------------------------------------------------------------------|
| Language     | Java 25                                                                         |
| Framework    | Spring Boot 4.0.6                                                               |
| API          | Spring Web MVC, Bean Validation, SpringDoc OpenAPI                              |
| Security     | Spring Security, OAuth2 Authorization Server, OAuth2 Resource Server, JWT, PKCE |
| Database     | PostgreSQL, Spring Data JPA, Hibernate                                          |
| Migrations   | Flyway                                                                          |
| File storage | Cloudinary                                                                      |
| Tooling      | Maven Wrapper, Docker, Docker Compose                                           |
| Testing      | JUnit, Spring Boot Test, Spring Security Test                                   |

## Domain Overview

The API models a real estate marketplace where public users can browse available properties, brokers can manage their own listings, and admins can manage users and view the full inventory.

Core domain concepts:

- `Property`: title, description, price, transaction type, category, area, room details, availability, address, broker owner, and images.
- `Image`: uploaded media linked to a property, including one primary image.
- `User`: authenticated account with `ROLE_ADMIN` or `ROLE_BROKER`.
- `Address`: structured address data with state and ZIP code normalization.

Supported property categories:

```text
APARTMENT, HOUSE, COMMERCIAL, LAND, STUDIO, FARM
```

Supported transaction types:

```text
SALE, RENT
```

## Architecture

```mermaid
flowchart LR
    Client[Client / Postman / Swagger UI] --> API[Spring Boot API]
    API --> Auth[OAuth2 Authorization Server]
    API --> Resource[JWT Resource Server]
    API --> Service[Service Layer]
    Service --> DB[(PostgreSQL)]
    Service --> Cloudinary[Cloudinary]
    Flyway[Flyway Migrations] --> DB
```

The project follows a layered structure:

```text
src/main/java/com/mlcdev/realestate
|-- config          # Security, OpenAPI and Cloudinary configuration
|-- controller      # REST endpoints
|-- dto             # Request and response models
|-- entities        # JPA domain entities
|-- exception       # Domain exceptions and API error handling
|-- init            # Admin and development data initialization
|-- mapper          # DTO/entity mapping
|-- repository      # Spring Data repositories
|-- security        # JWT utilities, custom principal and ownership checks
|-- service         # Business rules and transactions
|-- specifications  # Dynamic filtering specifications
|-- util            # JSON deserializers
`-- validation      # Custom validation annotations
```

## Security Model

The API uses Spring Authorization Server and protects business endpoints as a JWT Resource Server.

### Authentication Flow

1. The user starts an OAuth2 Authorization Code flow with PKCE.
2. The Authorization Server authenticates the user through Spring Security form login.
3. The client exchanges the authorization code plus PKCE verifier for tokens.
4. Protected requests send `Authorization: Bearer <access_token>`.
5. The Resource Server validates the JWT using the local JWKS endpoint.

### Roles

| Role          | Capabilities                                                                                              |
|---------------|-----------------------------------------------------------------------------------------------------------|
| Public        | List available properties, view property details, list property images, access health and OpenAPI docs    |
| `ROLE_BROKER` | Create properties, update own properties, upload images, set primary images, delete own properties/images |
| `ROLE_ADMIN`  | Inherits broker permissions, manages users, sees all properties, can act across broker-owned resources    |

### Authorization Details

- `ADMIN` implies `BROKER` through Spring Security role hierarchy.
- Access tokens include `authorities` and `username` claims.
- Visitors can only retrieve available properties.
- Any authenticated `BROKER` or `ADMIN` can retrieve unavailable properties, regardless of ownership.
- Property ownership restricts updates and deletions, not authenticated read access.
- Broker mutations are checked against property ownership, while `ADMIN` can act across broker-owned resources.
- Deactivating a user invalidates that user's persisted OAuth2 authorizations.
- Passwords are encoded with BCrypt.
- New user passwords must contain uppercase, lowercase, number, and special character.

## API Overview

Base URL for local development:

```text
http://localhost:8080
```

### Public Endpoints

| Method | Endpoint                                     | Description                    |
|--------|----------------------------------------------|--------------------------------|
| `GET`  | `/actuator/health`                           | Application health             |
| `GET`  | `/v3/api-docs`                               | OpenAPI JSON                   |
| `GET`  | `/v1/properties`                             | List available properties      |
| `GET`  | `/v1/properties/{propertyId}`                | Get property details           |
| `GET`  | `/v1/properties/{propertyId}/images`         | List property images           |
| `GET`  | `/v1/properties/{propertyId}/images/primary` | Get the primary property image |

### Auth and Discovery

| Method     | Endpoint                            | Description                             |
|------------|-------------------------------------|-----------------------------------------|
| `GET`      | `/.well-known/openid-configuration` | OpenID Connect discovery metadata       |
| `GET`      | `/oauth2/jwks`                      | JSON Web Key Set                        |
| `POST`     | `/oauth2/token`                     | Token exchange and refresh token flow   |
| `GET/POST` | `/logout`                           | Clears the authenticated server session |

### Broker/Admin Endpoints

| Method   | Endpoint                                               | Description                                     |
|----------|--------------------------------------------------------|-------------------------------------------------|
| `POST`   | `/v1/properties`                                       | Create a property                               |
| `PATCH`  | `/v1/properties/{propertyId}`                          | Partially update a property                     |
| `PATCH`  | `/v1/properties/{propertyId}/toggle-active`            | Toggle property availability                    |
| `DELETE` | `/v1/properties/{propertyId}`                          | Delete a property                               |
| `GET`    | `/v1/properties/all`                                   | List all properties, including unavailable ones |
| `POST`   | `/v1/properties/{propertyId}/images`                   | Upload property images                          |
| `PATCH`  | `/v1/properties/{propertyId}/images/{imageId}/primary` | Set the primary property image                  |
| `DELETE` | `/v1/properties/{propertyId}/images/{imageId}`         | Delete an image                                 |

### Authenticated User

| Method | Endpoint       | Description                           |
|--------|----------------|---------------------------------------|
| `GET`  | `/v1/users/me` | Return the current authenticated user |

### Admin Endpoints

| Method  | Endpoint                           | Description                       |
|---------|------------------------------------|-----------------------------------|
| `GET`   | `/v1/users`                        | List users with filters           |
| `GET`   | `/v1/users/{userId}`               | Get user details                  |
| `POST`  | `/v1/users`                        | Create a broker user              |
| `PATCH` | `/v1/users/{userId}`               | Partially update a user           |
| `PATCH` | `/v1/users/{userId}/toggle-active` | Toggle user active status         |
| `GET`   | `/v1/users/{brokerId}/properties`  | List properties owned by a broker |

## Filtering and Pagination

Property listing endpoints support pageable Spring parameters:

```text
page=0
size=20
sort=createdAt,desc
```

Available property filters:

```text
search
minPrice
maxPrice
minArea
maxArea
transactionType
category
minBedrooms
maxBedrooms
minBathrooms
minSuites
minParkingSpots
```

User listing supports:

```text
username
role
isActive
```

Example:

```http
GET /v1/properties?search=pool&transactionType=SALE&category=HOUSE&page=0&size=10&sort=price,asc
```

## Getting Started

### Prerequisites

- Java 25
- Docker and Docker Compose
- Maven is optional because the project includes `mvnw`
- A Cloudinary account for real image uploads
- OpenSSL for generating local RSA keys

### 1. Clone the repository

```bash
git clone https://github.com/MatheusLeiteCarneiro/real-estate.git
cd real-estate
```

### 2. Create the environment file

```bash
cp .env.example .env
```

Update the values in `.env` according to your local environment. At minimum, configure PostgreSQL, Cloudinary, admin credentials, and the authorization server URL.

```env
SPRING_PROFILES_ACTIVE=dev
POSTGRES_DB=realestate_db
POSTGRES_USER=admin
POSTGRES_PASSWORD=change-me
ADMIN_USERNAME=admin@example.com
ADMIN_PASSWORD=ChangeMe123!
CLOUDINARY_URL=cloudinary://your-api-key:your-api-secret@your-cloud-name
AUTHORIZATION_SERVER_URL=http://localhost:8080
```

### 3. Generate local JWT keys

```bash
mkdir -p secrets
openssl genrsa -out secrets/app.key 2048
openssl rsa -in secrets/app.key -pubout -out secrets/app.pub
```

### 4. Run with Docker Compose

```bash
docker compose up --build
```

The API will be available at:

```text
http://localhost:8080
```

Docker Compose starts:

- PostgreSQL 15
- The Spring Boot API
- A persistent `postgres_data` volume
- A health check before the API starts

### 5. Run locally with Maven

Start PostgreSQL separately, configure `.env`, then run:

```bash
./mvnw -pl api spring-boot:run -Dspring-boot.run.workingDirectory="$(pwd)"
```

The application automatically loads `.env` values during startup.

## Swagger UI

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

The OpenAPI configuration includes an OAuth2 Authorization Code flow with PKCE.

### Authenticated Endpoint Example

![Authenticated property creation endpoint in Swagger UI](docs/imgs/swagger-example.png)

For local development, use:

```text
Client ID: swagger-client
Scopes: openid profile
```

The Swagger redirect URI is configured through:

```env
SWAGGER_REDIRECT_URI=http://localhost:8080/swagger-ui/oauth2-redirect.html
```

## Postman

This repository includes:

```text
real-estate.postman_collection.json
real-estate.api.postman_environment.json
```

Recommended flow:

1. Import both files into Postman.
2. Select the `Real Estate - API` environment.
3. Update the credentials in the `Real Estate - API` environment.
4. Open the collection Authorization tab.
5. Confirm the grant type is `Authorization Code (With PKCE)`.
6. Click `Get New Access Token`.
7. Login with the configured admin credentials.
8. Click `Use Token`.

Protected requests inherit OAuth2 auth from the collection, so the `Authorization` header is generated by Postman.

## Useful Credentials for Local Development

The initial admin user is created from `.env`:

```env
ADMIN_USERNAME=admin@example.com
ADMIN_PASSWORD=ChangeMe123!
```

These are local development example credentials. Change them before running the project outside your machine.

## Environment Variables

Important variables:

| Variable                     | Purpose                                              |
|------------------------------|------------------------------------------------------|
| `SPRING_PROFILES_ACTIVE`     | Spring profile, usually `dev` locally                |
| `CLOUDINARY_URL`             | Cloudinary connection URL                            |
| `PROPERTY_IMAGE_FOLDER`      | Cloudinary folder for property images                |
| `MAX_FILE_SIZE`              | Maximum size for each uploaded file (default: 10MB)  |
| `MAX_REQUEST_SIZE`           | Maximum total size per request (default: 100MB)      |
| `POSTGRES_DB`                | Database name                                        |
| `POSTGRES_USER`              | Database user                                        |
| `POSTGRES_PASSWORD`          | Database password                                    |
| `DB_HOST`                    | Database host                                        |
| `DB_PORT`                    | Database port                                        |
| `ADMIN_USERNAME`             | Initial admin username                               |
| `ADMIN_PASSWORD`             | Initial admin password                               |
| `AUTHORIZATION_SERVER_URL`   | Issuer and OAuth2 base URL                           |
| `JWT_ACCESS_TOKEN_DURATION`  | Access token lifetime in seconds                     |
| `JWT_REFRESH_TOKEN_DURATION` | Refresh token lifetime in seconds                    |
| `POSTMAN_CLIENT_ID`          | OAuth2 client ID for Postman                         |
| `POSTMAN_REDIRECT_URI`       | OAuth2 redirect URI for Postman                      |
| `SWAGGER_CLIENT_ID`          | OAuth2 client ID for Swagger UI                      |
| `SWAGGER_REDIRECT_URI`       | OAuth2 redirect URI for Swagger UI                   |
| `SPA_CLIENT_ID`              | OAuth2 client ID for a production SPA                |
| `SPA_REDIRECT_URI`           | OAuth2 redirect URI for a production SPA             |
| `CORS_ALLOWED_ORIGINS`       | Allowed frontend origins                             |
| `JWT_PUBLIC_KEY`             | Public RSA key location                              |
| `JWT_PRIVATE_KEY`            | Private RSA key location                             |

Uploads are limited to 10MB per file and 100MB per multipart request. Keep `MAX_FILE_SIZE` compatible with your Cloudinary account limits.

## Database Migrations

Flyway manages the schema under:

```text
src/main/resources/db/migration
```

The migrations cover:

- Initial property and address schema
- Image metadata and primary image support
- User table and roles
- Broker-property ownership relationship
- Property enum/status refactors
- User timestamps
- OAuth2 authorization persistence schema

## Testing

Run the complete verification lifecycle with:

```bash
./mvnw clean verify
```

The current test structure includes coverage for:

- Application context
- Property mapping
- Property service behavior
- Broker ownership validation
- Strong password validation
- PostgreSQL integration tests using Testcontainers
- Flyway migration validation against a real PostgreSQL 15 container
- Property visibility and authorization integration tests
- Image upload rollback behavior

## Example Request

Create a property as an authenticated broker or admin:

```http
POST /v1/properties
Authorization: Bearer <access_token>
Content-Type: application/json
```

```json
{
  "title": "Modern family house",
  "description": "Spacious property near schools, parks, and local shops.",
  "price": 750000.00,
  "transactionType": "SALE",
  "category": "HOUSE",
  "suites": 1,
  "bedrooms": 3,
  "bathrooms": 2,
  "area": 120.50,
  "parkingSpots": 2,
  "address": {
    "street": "Main Street",
    "number": "100",
    "complement": "Apartment 12",
    "neighborhood": "Downtown",
    "city": "Sample City",
    "state": "CA",
    "zipCode": "90000000"
  }
}
```

## Portfolio Notes

This project covers:

- Secure API design with OAuth2, PKCE, JWT and role-based authorization
- Practical ownership rules for multi-user business domains
- Clear separation between controllers, services, repositories, mappers and DTOs
- Validation and normalized error responses
- Real database migration management instead of generated schemas
- Dockerized local development with PostgreSQL and health checks
- API testing support through Swagger UI and Postman

## Roadmap

Potential next improvements:

- Add deployment manifests for a cloud provider
- Add API rate limiting
- Add audit logging for admin actions
- Add frontend client using the existing OAuth2 SPA client configuration

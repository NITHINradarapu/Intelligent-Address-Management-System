# Intelligent Address Management System

A Spring Boot REST API project for managing customer address records.

## Project Status (Completed So Far)

The following parts are implemented:

- Spring Boot application setup
- Customer entity with JPA mapping (`customers` table)
- DTO-based request/response model
- Input validation for customer fields
- Customer CRUD APIs
- Service layer with business rules
- Email uniqueness check
- Global exception handling for:
  - Customer not found
  - Duplicate email
- MySQL configuration in `application.properties`

## Tech Stack

- Java 25
- Spring Boot 4.1.0
- Spring Web MVC
- Spring Data JPA
- MySQL
- Lombok
- Maven

## API Endpoints

Base URL: `/api/customers`

- `POST /api/customers` - Create customer
- `GET /api/customers` - Get all customers
- `GET /api/customers/{id}` - Get customer by ID
- `PUT /api/customers/{id}` - Update customer
- `DELETE /api/customers/{id}` - Delete customer

## Request Payload (Create/Update)

```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "phoneNumber": "9876543210"
}
```

Validation rules:

- `name` is required
- `email` is required and must be valid
- `phoneNumber` is required and must be exactly 10 digits

## Database Configuration

Configured in:

- `/src/main/resources/application.properties`

Update these values based on your local MySQL setup:

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

## How to Run

1. Create MySQL database:
   - `address_management_db`
2. Update DB credentials in `application.properties`
3. Run:

```bash
./mvnw spring-boot:run
```

## Basic Test

```bash
./mvnw test
```

## Pending / Next Improvements

- Add update timestamps using JPA auditing
- Add pagination and search/filter support
- Add unit and integration tests for controller/service layers
- Add API documentation (Swagger/OpenAPI)
- Add authentication and role-based access control
- Improve centralized validation error responses

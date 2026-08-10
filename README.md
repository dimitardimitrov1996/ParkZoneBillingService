# ParkZoneBillingService

## Repositories

- Billing microservice: [ParkZoneBillingService](https://github.com/dimitardimitrov1996/ParkZoneBillingService)
- Main application: [ParkZone](https://github.com/dimitardimitrov1996/ParkZone)

ParkZoneBillingService is a Spring Boot REST microservice responsible for invoice management in the ParkZone system.

The microservice is used by the main ParkZone application through OpenFeign. It handles invoice creation, invoice lookup, invoice payment, invoice update, invoice cancellation, and refunded invoice status handling.

## Tech Stack

- Java 21
- Spring Boot 3.4.0
- Spring Web
- Spring Data JPA
- Spring Security
- Spring Validation
- Hibernate
- MySQL
- H2 Database for tests
- Maven
- Lombok
- JUnit 5
- Mockito
- MockMvc

## Application Overview

ParkZoneBillingService is a separate application from the main ParkZone web application.

Its responsibility is to manage reservation invoices independently from the main application.

The main application creates and manages reservations, while this microservice manages the invoice lifecycle connected to those reservations.

## Main Features

- Create invoice
- Get invoice by reservation ID
- Update pending invoice
- Pay invoice
- Cancel invoice by reservation ID
- Mark paid cancelled invoice as refunded
- Protect all invoice endpoints with API key authentication
- Validate invoice request data
- Return structured error responses
- Log invoice operations and rejected business actions

## Invoice Statuses

Invoices can have the following statuses:

```text
PENDING
PAID
CANCELLED
REFUNDED
```

## Invoice Rules

### Creating an Invoice

An invoice can be created only if there is no existing invoice for the same reservation.

Required data:

- Reservation ID
- User ID
- Amount
- Currency

When an invoice is created, its initial status is:

```text
PENDING
```

### Updating an Invoice

Only pending invoices can be updated.

This is used when a ParkZone user edits a reservation that is still not paid. The main application recalculates the reservation price and sends the new invoice amount to the billing microservice.

Updated fields:

- Amount
- Currency

### Paying an Invoice

Only pending invoices can be paid.

When an invoice is paid:

- Status changes from `PENDING` to `PAID`
- `paidOn` timestamp is set

### Cancelling an Invoice

Invoices are cancelled by reservation ID.

Cancellation behavior:

- If the invoice is `PENDING`, it becomes `CANCELLED`
- If the invoice is `PAID`, it becomes `REFUNDED`
- If the invoice is already `CANCELLED` or `REFUNDED`, the request is rejected

## REST API

Base URL:

```text
http://localhost:8081/api/v1/invoices
```

### Create Invoice

```http
POST /api/v1/invoices
```

Request body:

```json
{
  "reservationId": "11111111-1111-1111-1111-111111111111",
  "userId": "22222222-2222-2222-2222-222222222222",
  "amount": 50.00,
  "currency": "EUR"
}
```

### Get Invoice by Reservation ID

```http
GET /api/v1/invoices/reservation/{reservationId}
```

### Update Invoice by Reservation ID

```http
PUT /api/v1/invoices/reservation/{reservationId}
```

Request body:

```json
{
  "amount": 120.00,
  "currency": "EUR"
}
```

### Pay Invoice

```http
PUT /api/v1/invoices/{invoiceId}/pay
```

### Cancel Invoice by Reservation ID

```http
PUT /api/v1/invoices/reservation/{reservationId}/cancel
```

## Security

The billing microservice is protected with API key authentication.

All invoice endpoints require the following request header:

```http
X-API-Key
```

Example:

```http
X-API-Key: your-api-key
```

If the API key is missing, the service returns:

```text
401 Unauthorized
```

If the API key is invalid, the service returns:

```text
403 Forbidden
```

## Communication with ParkZone

ParkZone communicates with this billing microservice through OpenFeign.

Main application repository:

[ParkZone](https://github.com/dimitardimitrov1996/ParkZone)

The main application uses the billing service for:

- Creating invoices when reservations are created
- Updating invoices when pending reservations are edited
- Getting invoice status for reservation history
- Paying invoices
- Cancelling invoices when reservations are cancelled

## Validation and Error Handling

The microservice uses DTO validation and custom exceptions.

Validation examples:

- Reservation ID is required
- User ID is required
- Amount is required
- Amount must be positive
- Currency is required

The application includes centralized exception handling through `GlobalExceptionHandler`.

Handled cases include:

- Validation errors
- Business rule violations
- Invalid API key
- Missing invoice
- Generic application exceptions

Error responses include information such as:

- Timestamp
- Status
- Error
- Message
- Path
- Field errors

## Logging

The service logs important invoice operations, including:

- Invoice creation
- Invoice update
- Invoice payment
- Invoice cancellation
- Rejected payment attempts
- Rejected update attempts
- Rejected cancellation attempts

## Database

The application uses MySQL for development/runtime and H2 for tests.

Example MySQL configuration:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/parkzone_billing?createDatabaseIfNotExist=true
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:1234}
```

Example test database configuration:

```properties
spring.datasource.url=jdbc:h2:mem:parkzone_billing_test;MODE=MYSQL
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=create-drop
```

## Environment Variables

The billing microservice supports the following environment variables:

```properties
DB_USERNAME=root
DB_PASSWORD=1234
BILLING_API_KEY=your-api-key
```

The value of `BILLING_API_KEY` must match the API key used by the main ParkZone application.

Example application property:

```properties
billing.service.api.key=${BILLING_API_KEY}
```

## Running the Application

Run the billing microservice before starting the main ParkZone application.

Start the application with Maven:

```bash
mvn spring-boot:run
```

Default URL:

```text
http://localhost:8081
```

Invoice API base URL:

```text
http://localhost:8081/api/v1/invoices
```

## Running Tests

Run all tests with:

```bash
mvn test
```

The project includes:

- Service tests
- Controller tests
- Security/API key tests
- Mapper tests
- Validation tests

The current test coverage is above the required 70% line coverage.

## Project Structure

```text
src/main/java/softuni/parkzonebillingservice
├── config
├── controller
├── exception
├── mapper
├── model
│   ├── dto
│   └── entity
├── repository
└── service
```

Layer responsibilities:

- `controller` - REST API endpoints
- `service` - invoice business logic
- `repository` - database access
- `mapper` - entity-to-response mapping
- `model.entity` - JPA entity
- `model.dto` - request and response DTOs
- `config` - security and API key configuration
- `exception` - custom exceptions and global exception handling

## Related Repositories

- Billing microservice: [ParkZoneBillingService](https://github.com/dimitardimitrov1996/ParkZoneBillingService)
- Main application: [ParkZone](https://github.com/dimitardimitrov1996/ParkZone)

## Author

Dimitar Dimitrov

GitHub: [dimitardimitrov1996](https://github.com/dimitardimitrov1996)
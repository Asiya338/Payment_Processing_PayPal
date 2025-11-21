# 🟦 Payment Processing Service — Java + Spring Boot (Microservices Project)

The **Payment Processing Service (PPS)** is a production-style Spring Boot microservice that manages the full **payment lifecycle** for orders and delegates real payment operations to the **PayPal Provider Service**.  
It stores transaction data, updates statuses, validates requests, and handles create → initiate → capture flows robustly with resilience, observability and JDBC-based persistence.

Implemented features include:

- 🧾 Create Payment (local transaction creation)  
- 🔁 Initiate Payment (calls PayPal Provider Service → Create Order)  
- ✅ Capture Payment (calls PayPal Provider Service → Capture Order)  
- 🗄️ MySQL persistence (Spring JDBC / NamedParameterJdbcTemplate)  
- 🔁 Resilience4j (retry + circuit breaker) for downstream calls  
- 📊 Micrometer (traceId, spanId) + Logback logging (file + console + rolling)  
- 📘 Swagger / OpenAPI documentation  
- 🚦 Spring Boot Actuator for monitoring  
- 🧰 Global exception handling with consistent `200xx` error codes

----------------------------

## 🧩 ✔️ Layered Architecture Used

```
src/main/java
└── com.hulkhiretech.payments
├── config # DB, RestClient, Resilience4j, Swagger, Micrometer configs
├── constants # Constant keys (statuses, error codes, provider URLs)
├── controller # REST API controllers (endpoints)
├── dao.impl # DAO implementations (Spring JDBC / NamedParameterJdbcTemplate)
├── dao.interfaces # DAO interfaces
├── dto # DTOs used across service layer
├── entity # DB entity models (used by DAO layer)
├── exception # Custom exceptions + Global @RestControllerAdvice
├── http # HTTP wrappers / response models used for downstream calls
├── paypalprovider # Client classes calling PayPal Provider Service
├── pojo # Request / Response POJOs (incoming/outgoing)
├── service # Service layer contracts
├── service.factory # Processor / factory for status-specific flows
├── service.helper # Reusable helpers used by services
├── service.impl # Service implementations
├── service.impl.statusProcessor # Status specific processors (CREATED, INITIATED, etc.)
└── util # Utility classes (UUID generator, validators, mappers)
```



----------------------------

## 🛠️ Tech Stack

| Layer | Technology |
|------:|------------|
| Language | Java 17 |
| Framework | Spring Boot |
| Persistence | MySQL (Spring JDBC, NamedParameterJdbcTemplate) |
| REST Client | Spring RestClient (RestTemplate / WebClient) |
| Resilience | Resilience4j (circuit breaker, retry, timeouts) |
| Logging | Logback (file + console, hourly rolling, max 20 files) |
| Observability | Micrometer (traceId, spanId) |
| Docs | Swagger / OpenAPI |
| Build | Maven |
| Utility | Lombok, devtools |

----------------------------

## 🔐 PayPal Provider Integration (Downstream)

This service **calls** your PayPal Provider Service (already built) for the real PayPal operations:

- **Create Order** → `POST {paypal-provider.base-url}/orders` (used in *Initiate*)  
- **Capture Order** → `POST {paypal-provider.base-url}/orders/{orderId}/capture` (used in *Capture*)

Configuration in `application.properties` / `application-*.yml`:

```properties
paypal.provider.base-url=http://localhost:8083
paypal.provider.create-endpoint=/orders
paypal.provider.capture-endpoint=/orders/{orderId}/capture

```

-------------------------


This project uses a clean **layered architecture** (Controller → Service → DAO/Repository) to keep responsibilities separated and code testable.

---

## ✔️ Standard Package Used


Architecture of Status Processor Flow

```
Controller
    ↓
Service Layer
    ↓
PaymentStatusProcessor (Main Orchestrator)
    ↓
TransactionStatusProcessorFactory
    ↓
TransactionStatusProcessor (Interface)
    ↙        ↓         ↘
CreatedStatusProcessor   InitiatedStatusProcessor   FailedStatusProcessor  ...

```
Factory Pattern (Status Processing)

Implemented a Factory Pattern to manage payment status workflows.
PaymentStatusProcessor invokes TransactionStatusProcessorFactory, which returns the correct status processor (e.g., CreatedStatusProcessor, InitiatedStatusProcessor, FailedStatusProcessor, etc.).
This ensures each status updates the database through its own dedicated processor class during create, initiate, and capture flows.

----------------------------

Core Payment Flows
✔️ 1. Create Payment
```
Endpoint

POST /payments
```

Behavior

Validates incoming request (amount, currency, paymentType, paymentMethod, etc.)

Inserts transaction into DB → status = CREATED

Generates transactionReference (UUID, unique - unpredictable)

Returns PaymentResponse with transactionReference and initial status

DB update

transaction table row inserted -> set CREATED

✔️ 2. Initiate Payment
```
Endpoint

POST /payments/{txnReference}/initiate
```

Behavior

Fetches transaction by transactionReference

Updates status = INITIATED

Prepares provider payload and calls PayPal Provider Service → Create Order

Handles downstream response:

Success → update status = PENDING, set providerReference = orderId

Failure → update status = FAILED, throw business exception

No response / timeout → update status = FAILED, throw exception (global handler will format response)

All exceptions have custom 200xx error codes and meaningful messages

Notes

All calls logged with traceId/spanId for distributed tracing

Retries are attempted per Resilience4j policy; final failure handled by fallback

✔️ 3. Capture Payment
```
Endpoint

POST /payments/{txnReference}/capture
```

Behavior

Fetches transaction by transactionReference

Updates status → APPROVED (business pre-step before capture)

Calls PayPal Provider Service → Capture Order of PayPal 

Handles downstream response:

Success → update status = SUCCESS

Failure or No response → DO NOT change to FAILED

Rationale: customer has approved payment and money may already be debited; to avoid inconsistency we throw an exception and let reconciliation handle it.

Exception thrown for downstream failures; global exception handler returns structured error with 200xx code

-----------------------------------

🗄️ Database Design (MySQL)

Database: payments

Important Tables

transaction — main transactional table

columns: id, txnReference (UUID), provideReference, amount, currency, status, paymentTypeId, paymentMethodId, createdAt, retryCount

payment_status — master table (CREATED, INITIATED, PENDING, APPROVED, SUCCESS, FAILED)

payment_type — APM (master data)

payment_provider — paypal (master data)

---------------------------

Persistence

Spring JDBC with NamedParameterJdbcTemplate

DAO interfaces + DAO implementations in dao.*

DDL + DML scripts to be executed before application start (to seed master tables)

🧰 Error Handling & Codes

Global exception handler (@RestControllerAdvice) returns consistent JSON:

{
  "errorCode": "20014",
  "errorMessage": "Failed to initiate payment - PayPal Provider timed out"
}


All service-level errors use 200xx prefix for clear traceability across logs and support tickets.

Validation errors, DB errors, and downstream errors have structured messages and traceable codes.

---------------------------

🔄 Resilience & Fault Tolerance

Resilience4j Circuit Breaker wraps calls to PayPal Provider Service

Configured with:

failure threshold

wait duration (open → half-open)

retry attempts

timeouts

Fallback methods return meaningful error codes and trigger alerts/logs for manual reconciliation when required

---------------------------

🔍 Logging & Observability

Logback configured (logback-spring.xml) to log to file + console

Hourly rolling policy, retain max 20 files

Log pattern includes traceId and spanId (Micrometer integration)

Micrometer provides trace and metrics; integrated with Actuator

------------------------------

📘 Swagger & Actuator

Swagger UI

http://localhost:8080/swagger-ui/index.html


OpenAPI JSON

http://localhost:8080/v3/api-docs


Actuator Endpoints

/actuator/health

/actuator/info

/actuator/metrics

/actuator/loggers

/actuator/env

/actuator/beans

/actuator/mappings

-------------------------

🚀 How to Run (Local)

Run MySQL & seed DB

Execute provided ddl/*.sql and dml/*.sql to create tables and master data

Set application properties / environment

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/payments
spring.datasource.username=root
spring.datasource.password=your_password

# PayPal Provider (downstream)
PAYPAL_PROVIDER_BASE_URL=http://localhost:8083

# (Optional) Spring profiles, logging, etc.
-------------------

Build
```
mvn clean package
```

Run
```
java -jar target/payment-processing-service.jar
```

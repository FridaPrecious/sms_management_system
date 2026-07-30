# SMS Microservices Project

A Spring Boot microservices application for processing bulk SMS messages using asynchronous communication with RabbitMQ. The system allows users to upload an Excel file containing phone numbers and SMS messages, validates Kenyan phone numbers, queues valid messages for processing, and simulates SMS delivery while maintaining separate databases for each microservice. The project demonstrates core microservices concepts such as service separation, asynchronous messaging, batch processing, and database-per-service architecture. :contentReference[oaicite:0]{index=0}

## Features

- Upload SMS records from an Excel (.xlsx) file
- Validate Kenyan phone numbers
- Reject invalid phone numbers
- Batch valid SMS messages for efficient processing
- Asynchronous communication using RabbitMQ
- Independent SMS Listener and SMS Sender microservices
- Separate H2 databases for each microservice
- RESTful APIs for SMS processing
- SMS delivery simulation with delivery logging :contentReference[oaicite:1]{index=1}

## System Architecture

The application consists of two independent microservices:

- **SMS Listener Service**
  - Accepts Excel file uploads
  - Reads and validates phone numbers
  - Stores validation results
  - Batches valid SMS records
  - Publishes message batches to RabbitMQ

- **SMS Sender Service**
  - Consumes message batches from RabbitMQ
  - Simulates SMS delivery
  - Stores delivery logs
  - Records successful and failed deliveries

RabbitMQ acts as the communication layer between the two services, enabling asynchronous message processing. :contentReference[oaicite:2]{index=2}

## Technologies Used

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring AMQP
- RabbitMQ
- Apache POI
- H2 Database
- Maven
- Lombok :contentReference[oaicite:3]{index=3}

## Project Structure

```
sms-microservices/
├── sms-listener/
└── sms-sender/
```

Each microservice is developed independently with its own configuration, REST controllers, services, repositories, models, and database.

## How It Works

1. A user uploads an Excel file containing phone numbers and SMS messages.
2. The Listener Service validates each phone number.
3. Invalid records are marked and stored in the Listener database.
4. Valid SMS messages are grouped into configurable batches.
5. The batches are published to a RabbitMQ queue.
6. The Sender Service consumes the queued batches.
7. SMS delivery is simulated.
8. Delivery results are stored in the Sender database. :contentReference[oaicite:4]{index=4}

## Phone Number Validation Rules

A phone number is considered valid if it:

- Starts with `254`
- Contains exactly 12 digits
- Contains only numeric characters

Example:

| Phone Number | Status |
|--------------|--------|
|254712345678|Valid|
|0712345678|Invalid| :contentReference[oaicite:5]{index=5}

## Running the Application

### Prerequisites

- Java 21
- Maven
- RabbitMQ Server

### Start RabbitMQ

Ensure RabbitMQ is running before starting the services.

### Run the Listener Service

```bash
cd sms-listener
mvn spring-boot:run
```

### Run the Sender Service

```bash
cd sms-sender
mvn spring-boot:run
```

## REST API

### SMS Listener Service

| Method | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/sms/upload` | Upload an Excel file |
| GET | `/api/sms/validate/{phoneNumber}` | Validate a phone number |
| GET | `/api/sms/requests` | Retrieve uploaded requests |

### SMS Sender Service

| Method | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/sms/send` | Send an SMS |
| GET | `/api/sms/logs` | Retrieve SMS delivery logs | :contentReference[oaicite:6]{index=6}

## Microservices Concepts Demonstrated

- Service Separation
- Database per Service
- Asynchronous Communication
- Message Queuing
- Batch Processing
- RESTful APIs
- Fault Tolerance :contentReference[oaicite:7]{index=7}

## Future Improvements

- Integrate a production SMS gateway such as Africa's Talking or Twilio
- Docker and Docker Compose deployment
- Kubernetes orchestration
- JWT authentication and authorization
- Retry mechanism for failed deliveries
- Monitoring and metrics dashboard
- PostgreSQL or MySQL support
- Comprehensive unit and integration testing

## Author

**Precious Anyangu**



## License

This project is intended for educational purposes.
# Setup and Run Instructions

## Prerequisites
- Java 17
- Maven

## Running the Application
1. Navigate to the project root:
   ```bash
   cd /Users/vngautam/Documents/BookingService
   ```
2. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

## Running the Tests
Execute the following command to run the automated test suite:
```bash
./mvnw test
```

## API Endpoints
- `GET http://localhost:8080/bookings` - List all bookings.
- `POST http://localhost:8080/book/appointment` - Create a booking.
  - **Payload**:
    ```json
    {
      "bookingId": "B1",
      "providerId": "P1",
      "bookingName": "Client Name",
      "startTime": "2026-11-01T10:00:00Z",
      "endTime": "2026-11-01T11:00:00Z"
    }
    ```
- `DELETE http://localhost:8080/delete/appointment/{id}` - Cancel a booking.

Timestamps are parsed as `Instant`, so include `Z` for UTC or provide an explicit
offset. The service uses an in-memory store; bookings are lost when it stops.

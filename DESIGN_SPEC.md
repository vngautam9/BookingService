# Design Specification: Appointment Booking Service

## Service Boundary
The service provides a bounded slice for conflict-safe appointment booking and cancellation for providers. It focuses exclusively on the invariant that a provider cannot have overlapping active appointments.

## Data Model
- **Entity**: `BookingModel`
    - `bookingId` (String, PK): Unique identifier for the booking.
    - `providerId` (String): Identifier for the service provider.
    - `bookingName` (String): Name of the booking/client.
- `startTime` (`Instant`): Start of the appointment as an absolute UTC instant.
- `endTime` (`Instant`): End of the appointment as an absolute UTC instant.
    - `status` (Enum): `ACTIVE` or `CANCELLED`.

## API Design
- `GET /bookings`: Returns a list of all bookings.
- `POST /book/appointment`: Creates a new booking if no conflict exists.
    - **Input**: `BookingModel` JSON.
    - **Conflict Check**: Rejects if any `ACTIVE` booking for the same `providerId` overlaps the requested interval.
- `DELETE /delete/appointment/{id}`: Cancels an existing booking.
    - **Action**: Sets status to `CANCELLED`.

## Booking Invariant
An appointment is considered overlapping if:
`(ExistingStartTime < NewEndTime) AND (ExistingEndTime > NewStartTime)`

Adjacent appointments (where `ExistingEndTime == NewStartTime`) are explicitly allowed.

## Persistence & Concurrency
- **Persistence**: Process-local in-memory `ConcurrentHashMap` behind `BookingRepository`. Data is lost when the application stops.
- **Concurrency**: `BookingService` synchronizes booking and cancellation operations on the Spring singleton service instance, making the check-and-save sequence atomic within one application process. This does not coordinate multiple application instances; production multi-instance deployments need shared persistence and a transaction or lock that protects the provider/time-range invariant.

## Assumptions
- All timestamps are handled in UTC.
- API timestamps must include a UTC designator (`Z`) or an explicit offset; `Instant` normalizes them to UTC.
- Duration must be strictly positive (`endTime > startTime`).
- A cancelled booking no longer blocks the provider's time.

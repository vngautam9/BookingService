# AI Prompt and Use Record

## Activity 1: Requirements review

- **Prompt**: “I have given you a document regarding a project. Please go through that thoroughly and then check the code i have written in the BookingService folder for any enhancements or fixes. Also can you generate the unit test cases.”
- **AI summary**: The assessment requires conflict-safe booking and cancellation, UTC timestamps, positive durations, adjacency support, provider-scoped conflicts, focused automated tests, and a record of AI use and verification. Existing code uses `Instant`, an in-memory `ConcurrentHashMap` repository, and synchronized booking/cancellation methods.
- **Decision**: Kept the existing service boundary and in-memory persistence approach. Did not add a database or unrelated features.
- **Verification**: Read the full two-page assessment and compared its acceptance criteria with the service, model, repository, controller, and project documentation.

## Activity 2: Review and documentation corrections

- **Prompt**: Same project-review request above.
- **AI suggestions**: The overlap predicate is correct and allows adjacent appointments. Clarify the `Instant` timestamp format in the setup example and make the design specification match the actual in-memory storage and process-local synchronization. Document that synchronization does not coordinate multiple application instances.
- **Decision**: Accepted. Updated the timestamp example to include `Z`; corrected the design specification and documented the in-memory persistence and concurrency limit.
- **Files**: `SETUP.md`, `DESIGN_SPEC.md`.
- **Verification**: Checked the documentation against the actual `BookingService`, `BookingRepository`, and `BookingModel` implementations.

## Activity 3: Unit tests

- **Prompt**: “Also can you generate the unit test cases.”
- **AI suggestions**: Add unit tests for successful booking, overlapping bookings, provider isolation, adjacency, cancellation/rebooking, duplicate IDs, invalid input, listing, and concurrent conflicting requests.
- **Decision**: Accepted and implemented these cases in `BookingServiceTest`. Used the concrete in-memory repository so the tests exercise service and repository behavior together.
- **Verification**: Ran `./mvnw test`: 11 tests passed, with 0 failures, 0 errors, and 0 skipped.

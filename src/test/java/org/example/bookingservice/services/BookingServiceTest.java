package org.example.bookingservice.services;

import org.example.bookingservice.model.BookingModel;
import org.example.bookingservice.model.BookingModel.BookingStatus;
import org.example.bookingservice.repositories.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

class BookingServiceTest {

    private static final Instant START = Instant.parse("2026-11-01T10:00:00Z");
    private BookingRepository repository;
    private BookingService service;

    @BeforeEach
    void setUp() {
        repository = new BookingRepository();
        service = new BookingService(repository);
    }

    @Test
    void booksValidAppointmentAsActive() {
        BookingModel request = booking("B1", "P1", START, START.plusSeconds(3600));
        request.setStatus(BookingStatus.CANCELLED);

        BookingModel saved = service.bookAppointment(request);

        assertSame(request, saved);
        assertEquals(BookingStatus.ACTIVE, saved.getStatus());
        assertEquals(saved, repository.findById("B1").orElseThrow());
    }

    @Test
    void rejectsOverlappingActiveAppointmentForSameProvider() {
        service.bookAppointment(booking("B1", "P1", START, START.plusSeconds(3600)));

        assertThrows(IllegalStateException.class,
                () -> service.bookAppointment(booking("B2", "P1", START.plusSeconds(1800), START.plusSeconds(5400))));
        assertFalse(repository.existsById("B2"));
    }

    @Test
    void allowsOverlappingAppointmentsForDifferentProviders() {
        service.bookAppointment(booking("B1", "P1", START, START.plusSeconds(3600)));

        BookingModel saved = service.bookAppointment(
                booking("B2", "P2", START.plusSeconds(1800), START.plusSeconds(5400)));

        assertEquals(BookingStatus.ACTIVE, saved.getStatus());
        assertTrue(repository.existsById("B2"));
    }

    @Test
    void allowsAdjacentAppointments() {
        service.bookAppointment(booking("B1", "P1", START, START.plusSeconds(3600)));

        BookingModel saved = service.bookAppointment(
                booking("B2", "P1", START.plusSeconds(3600), START.plusSeconds(7200)));

        assertTrue(repository.existsById("B2"));
        assertEquals(BookingStatus.ACTIVE, saved.getStatus());
    }

    @Test
    void cancelledAppointmentNoLongerBlocksItsTime() {
        service.bookAppointment(booking("B1", "P1", START, START.plusSeconds(3600)));
        service.cancelAppointment("B1");

        BookingModel replacement = service.bookAppointment(
                booking("B2", "P1", START, START.plusSeconds(3600)));

        assertEquals(BookingStatus.CANCELLED,
                repository.findById("B1").orElseThrow().getStatus());
        assertEquals(BookingStatus.ACTIVE, replacement.getStatus());
    }

    @Test
    void rejectsDuplicateBookingId() {
        service.bookAppointment(booking("B1", "P1", START, START.plusSeconds(3600)));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.bookAppointment(booking("B1", "P2", START.plusSeconds(7200), START.plusSeconds(10800))));

        assertTrue(error.getMessage().contains("already exists"));
        assertEquals(1, service.getAllBookings().size());
    }

    @Test
    void rejectsNullBookingAndMissingIdentifiersOrTimesWithoutSaving() {
        assertThrows(IllegalArgumentException.class, () -> service.bookAppointment(null));
        assertInvalid(booking(" ", "P1", START, START.plusSeconds(3600)));
        assertInvalid(booking("B1", " ", START, START.plusSeconds(3600)));
        assertInvalid(booking("B1", "P1", null, START.plusSeconds(3600)));
        assertInvalid(booking("B1", "P1", START, null));
        assertTrue(service.getAllBookings().isEmpty());
    }

    @Test
    void rejectsZeroOrNegativeDurationWithoutSaving() {
        assertInvalid(booking("B1", "P1", START, START));
        assertInvalid(booking("B2", "P1", START.plusSeconds(60), START));
        assertTrue(service.getAllBookings().isEmpty());
    }

    @Test
    void cancellationValidatesIdAndExistingStatus() {
        assertThrows(IllegalArgumentException.class, () -> service.cancelAppointment(null));
        assertThrows(IllegalArgumentException.class, () -> service.cancelAppointment(" "));
        assertThrows(IllegalArgumentException.class, () -> service.cancelAppointment("missing"));

        service.bookAppointment(booking("B1", "P1", START, START.plusSeconds(3600)));
        service.cancelAppointment("B1");
        assertThrows(IllegalArgumentException.class, () -> service.cancelAppointment("B1"));
    }

    @Test
    void returnsAllBookings() {
        service.bookAppointment(booking("B1", "P1", START, START.plusSeconds(3600)));
        service.bookAppointment(booking("B2", "P2", START, START.plusSeconds(3600)));

        List<BookingModel> bookings = service.getAllBookings();

        assertEquals(2, bookings.size());
        assertTrue(bookings.stream().anyMatch(b -> "B1".equals(b.getBookingId())));
        assertTrue(bookings.stream().anyMatch(b -> "B2".equals(b.getBookingId())));
    }

    @Test
    void concurrentOverlappingRequestsAllowOnlyOneBooking() throws Exception {
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> first = pool.submit(() -> attemptConcurrentBooking("B1", ready, start));
            Future<Boolean> second = pool.submit(() -> attemptConcurrentBooking("B2", ready, start));
            ready.await();
            start.countDown();

            assertNotEquals(first.get(), second.get());
            assertEquals(1, service.getAllBookings().size());
        } finally {
            pool.shutdownNow();
        }
    }

    private boolean attemptConcurrentBooking(String id, CountDownLatch ready, CountDownLatch start)
            throws Exception {
        ready.countDown();
        start.await();
        try {
            service.bookAppointment(booking(id, "P1", START, START.plusSeconds(3600)));
            return true;
        } catch (IllegalStateException conflict) {
            return false;
        }
    }

    private void assertInvalid(BookingModel request) {
        assertThrows(IllegalArgumentException.class, () -> service.bookAppointment(request));
        assertFalse(repository.existsById(request.getBookingId()));
    }

    private static BookingModel booking(String id, String provider, Instant start, Instant end) {
        return new BookingModel(id, provider, "Client", start, end, null);
    }
}

package org.example.bookingservice.services;

import org.example.bookingservice.model.BookingModel;
import org.example.bookingservice.model.BookingModel.BookingStatus;
import org.example.bookingservice.repositories.BookingRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public synchronized BookingModel bookAppointment(BookingModel booking) {

        validateBooking(booking);

        booking.setStatus(BookingStatus.ACTIVE);

        if (bookingRepository.existsById(booking.getBookingId())) {
            throw new IllegalArgumentException(
                    "Booking ID already exists: " + booking.getBookingId());
        }

        List<BookingModel> existingBookings =
                bookingRepository.findByProviderId(booking.getProviderId());

        for (BookingModel existing : existingBookings) {

            if (existing.getStatus() != BookingStatus.ACTIVE) {
                continue;
            }

            boolean overlaps =
                    existing.getStartTime().isBefore(booking.getEndTime())
                            && booking.getStartTime().isBefore(existing.getEndTime());

            if (overlaps) {
                throw new IllegalStateException(
                        "Provider already has an appointment during this time.");
            }
        }

        bookingRepository.save(booking);

        return booking;
    }

    public synchronized void cancelAppointment(String bookingId) {

        if (bookingId == null || bookingId.isBlank()) {
            throw new IllegalArgumentException("Booking ID is required.");
        }

        BookingModel booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Booking not found: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Booking is already cancelled.");
        }

        booking.setStatus(BookingStatus.CANCELLED);

        bookingRepository.save(booking);
    }

    public List<BookingModel> getAllBookings() {
        return bookingRepository.findAll();
    }

    private void validateBooking(BookingModel booking) {

        if (booking == null) {
            throw new IllegalArgumentException("Booking request is required.");
        }

        if (booking.getBookingId() == null
                || booking.getBookingId().isBlank()) {
            throw new IllegalArgumentException("Booking ID is required.");
        }

        if (booking.getProviderId() == null
                || booking.getProviderId().isBlank()) {
            throw new IllegalArgumentException("Provider ID is required.");
        }

        Instant start = booking.getStartTime();
        Instant end = booking.getEndTime();

        if (start == null || end == null) {
            throw new IllegalArgumentException(
                    "Start time and end time are required.");
        }

        if (!end.isAfter(start)) {
            throw new IllegalArgumentException(
                    "End time must be after start time.");
        }
    }
}
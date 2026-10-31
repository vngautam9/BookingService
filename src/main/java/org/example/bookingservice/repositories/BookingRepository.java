package org.example.bookingservice.repositories;

import org.example.bookingservice.model.BookingModel;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class BookingRepository {

    private final ConcurrentHashMap<String, BookingModel> bookings =
            new ConcurrentHashMap<>();

    public void save(BookingModel booking) {
        bookings.put(booking.getBookingId(), booking);
    }

    public Optional<BookingModel> findById(String bookingId) {
        return Optional.ofNullable(bookings.get(bookingId));
    }

    public List<BookingModel> findAll() {
        return List.copyOf(bookings.values());
    }

    public boolean existsById(String bookingId) {
        return bookings.containsKey(bookingId);
    }

    public List<BookingModel> findByProviderId(String providerId) {
        return bookings.values()
                .stream()
                .filter(booking ->
                        booking.getProviderId().equals(providerId))
                .toList();
    }
}
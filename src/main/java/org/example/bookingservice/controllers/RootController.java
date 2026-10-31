package org.example.bookingservice.controllers;

import org.example.bookingservice.model.BookingModel;
import org.example.bookingservice.services.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RootController {

    private final BookingService bookingService;

    public RootController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/bookings")
    public List<BookingModel> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @PostMapping("/book/appointment")
    public ResponseEntity<?> bookAppointment(
            @RequestBody BookingModel booking) {

        try {
            BookingModel created =
                    bookingService.bookAppointment(booking);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(created);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());

        } catch (IllegalStateException e) {

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/appointment/{id}")
    public ResponseEntity<?> cancelAppointment(
            @PathVariable String id) {

        try {
            bookingService.cancelAppointment(id);

            return ResponseEntity.ok(
                    "Appointment cancelled successfully.");

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @GetMapping("/health")
    public String getInfo() {
        return "Booking Service is running.";
    }
}
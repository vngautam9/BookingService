package org.example.bookingservice.model;

import java.time.Instant;

public class BookingModel {

    private String bookingId;
    private String providerId;
    private String bookingName;
    private Instant startTime;
    private Instant endTime;
    private BookingStatus status;

    public enum BookingStatus {
        ACTIVE,
        CANCELLED
    }

    public BookingModel() {
    }

    public BookingModel(
            String bookingId,
            String providerId,
            String bookingName,
            Instant startTime,
            Instant endTime,
            BookingStatus status) {

        this.bookingId = bookingId;
        this.providerId = providerId;
        this.bookingName = bookingName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getBookingName() {
        return bookingName;
    }

    public void setBookingName(String bookingName) {
        this.bookingName = bookingName;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}
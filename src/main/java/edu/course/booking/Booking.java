package edu.course.booking;

public record Booking(
        BookingRequest request,
        long totalAmountInCents,
        String transactionId) {
}

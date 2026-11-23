package edu.course.booking;

public record BookingResult(Status status, String transactionId) {
    public enum Status {
        SUCCESS,
        INVALID_REQUEST,
        SOLD_OUT,
        PAYMENT_FAILED
    }
}

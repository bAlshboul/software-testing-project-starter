package edu.course.booking;

import java.util.Objects;

public final class BookingManager {
    private final IPaymentGateway paymentGateway;
    private final INotificationService notificationService;
    private final IEventRepository eventRepository;

    public BookingManager(
            IPaymentGateway paymentGateway,
            INotificationService notificationService,
            IEventRepository eventRepository) {
        this.paymentGateway = Objects.requireNonNull(paymentGateway);
        this.notificationService = Objects.requireNonNull(notificationService);
        this.eventRepository = Objects.requireNonNull(eventRepository);
    }

    

public BookingResult book(BookingRequest request) {
if (request.ticketQuantity() <= 0) {
    return new BookingResult(
            BookingResult.Status.INVALID_REQUEST,
            null
    );
}
if (eventRepository.isSoldOut(request.eventId())) {
    return new BookingResult(
            BookingResult.Status.SOLD_OUT,
            null
    );
}
    long totalAmountInCents =
            (long) request.ticketQuantity() * request.pricePerTicketInCents();

    String transactionId = paymentGateway
            .processPayment(request.customerId(), totalAmountInCents)
            .orElseThrow();

    Booking booking = new Booking(
            request,
            totalAmountInCents,
            transactionId
    );

    eventRepository.saveBooking(booking);
    notificationService.sendConfirmation(booking);

    return new BookingResult(
            BookingResult.Status.SUCCESS,
            transactionId
    );
}

}

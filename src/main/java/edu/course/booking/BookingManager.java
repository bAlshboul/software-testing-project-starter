package edu.course.booking;

import java.util.Objects;
import java.util.Optional;
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

    if (request == null
            || request.customerId() == null
            || request.customerId().isBlank()
            || request.eventId() == null
            || request.eventId().isBlank()
            || request.ticketQuantity() < 1
            || request.ticketQuantity() > 5
            || request.pricePerTicketInCents() <= 0) {

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
            (long) request.ticketQuantity()
            * request.pricePerTicketInCents();

    Optional<String> paymentResult = paymentGateway.processPayment(
            request.customerId(),
            totalAmountInCents
    );

    

if (paymentResult.isEmpty()) {
    return new BookingResult(
            BookingResult.Status.PAYMENT_FAILED,
            null
    );
}

String transactionId = paymentResult.orElseThrow();

if (transactionId.isBlank()) {
    return new BookingResult(
            BookingResult.Status.PAYMENT_FAILED,
            null
    );
}


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

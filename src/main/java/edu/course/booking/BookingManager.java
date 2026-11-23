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
        // Add each booking behavior as its first test is developed.
        throw new UnsupportedOperationException("Booking workflow has not been implemented");
    }
}

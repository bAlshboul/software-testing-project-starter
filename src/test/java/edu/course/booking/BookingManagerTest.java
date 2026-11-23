
package edu.course.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingManagerTest {

    @Mock
    private IPaymentGateway paymentGateway;

    @Mock
    private INotificationService notificationService;

    @Mock
    private IEventRepository eventRepository;

    private BookingManager bookingManager;

    @BeforeEach
    void setUp() {
        bookingManager = new BookingManager(
                paymentGateway,
                notificationService,
                eventRepository
        );
    }

    @Test
    void successfulBookingShouldProcessPaymentSaveBookingAndSendConfirmation() {

        BookingRequest request =
                new BookingRequest("C001", "E001", 2, 1500);

                when(paymentGateway.processPayment("C001", 3000L))
                .thenReturn(Optional.of("TXN001"));

        BookingResult result = bookingManager.book(request);

        assertEquals(BookingResult.Status.SUCCESS, result.status());
        assertEquals("TXN001", result.transactionId());

        Booking expectedBooking =
                new Booking(request, 3000L, "TXN001");

        verify(eventRepository).saveBooking(expectedBooking);
        verify(notificationService).sendConfirmation(expectedBooking);
    }
}

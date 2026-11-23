
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import java.util.stream.Stream;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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

        
verify(eventRepository).isSoldOut("E001");
verify(paymentGateway).processPayment("C001", 3000L);
verify(eventRepository).saveBooking(expectedBooking);
verify(notificationService).sendConfirmation(expectedBooking);

verifyNoMoreInteractions(
        eventRepository,
        paymentGateway,
        notificationService
);

    }

@Test
void bookingWithZeroTicketsShouldBeRejected() {
    BookingRequest request =
            new BookingRequest("C001", "E001", 0, 1500);

    BookingResult result = bookingManager.book(request);

    assertEquals(
            BookingResult.Status.INVALID_REQUEST,
            result.status()
    );

    verifyNoInteractions(
            paymentGateway,
            eventRepository,
            notificationService
    );
}

@Test
void soldOutEventShouldRejectBookingWithoutPayment() {
    BookingRequest request =
            new BookingRequest("C001", "E001", 2, 1500);

    when(eventRepository.isSoldOut("E001"))
            .thenReturn(true);

    BookingResult result = bookingManager.book(request);

    assertEquals(
            BookingResult.Status.SOLD_OUT,
            result.status()
    );

    assertEquals(null, result.transactionId());

    verify(eventRepository).isSoldOut("E001");

    verifyNoInteractions(
            paymentGateway,
            notificationService
    );

    verify(eventRepository, never()).saveBooking(any());
verifyNoMoreInteractions(eventRepository);
}


@Test
void failedPaymentShouldNotSaveBookingOrSendConfirmation() {
    BookingRequest request =
            new BookingRequest("C001", "E001", 2, 1500);

    when(paymentGateway.processPayment("C001", 3000L))
            .thenReturn(Optional.empty());

    BookingResult result = bookingManager.book(request);

    assertEquals(
            BookingResult.Status.PAYMENT_FAILED,
            result.status()
    );
    assertEquals(null, result.transactionId());

    verify(eventRepository).isSoldOut("E001");
    verify(paymentGateway).processPayment("C001", 3000L);

    verify(eventRepository, never()).saveBooking(any());
    verifyNoInteractions(notificationService);

    verifyNoMoreInteractions(eventRepository, paymentGateway);
}

@ParameterizedTest(name = "{0}")
@MethodSource("invalidBookingRequests")
void invalidBookingRequestShouldBeRejected(
        String scenario,
        BookingRequest request) {

    BookingResult result = bookingManager.book(request);

    assertEquals(
            BookingResult.Status.INVALID_REQUEST,
            result.status()
    );

    assertEquals(null, result.transactionId());

    verifyNoInteractions(
            paymentGateway,
            eventRepository,
            notificationService
    );
}

static Stream<Arguments> invalidBookingRequests() {
    return Stream.of(
            Arguments.of(
                    "Null customer ID",
                    new BookingRequest(null, "E001", 2, 1500)),
            Arguments.of(
                    "Empty customer ID",
                    new BookingRequest("", "E001", 2, 1500)),
            Arguments.of(
                    "Whitespace customer ID",
                    new BookingRequest("   ", "E001", 2, 1500)),
            Arguments.of(
                    "Null event ID",
                    new BookingRequest("C001", null, 2, 1500)),
            Arguments.of(
                    "Empty event ID",
                    new BookingRequest("C001", "", 2, 1500)),
            Arguments.of(
                    "Whitespace event ID",
                    new BookingRequest("C001", "   ", 2, 1500)),
            Arguments.of(
                    "Zero tickets",
                    new BookingRequest("C001", "E001", 0, 1500)),
            Arguments.of(
                    "Negative tickets",
                    new BookingRequest("C001", "E001", -1, 1500)),
            Arguments.of(
                    "More than five tickets",
                    new BookingRequest("C001", "E001", 6, 1500)),
            Arguments.of(
                    "Zero ticket price",
                    new BookingRequest("C001", "E001", 2, 0)),
            Arguments.of(
                    "Negative ticket price",
                    new BookingRequest("C001", "E001", 2, -100))
    );
}

@ParameterizedTest(name = "{0}")
@MethodSource("validBoundaryRequests")
void validBoundaryBookingsShouldCalculateCorrectTotal(
        String scenario,
        int quantity,
        int price,
        long expectedTotal) {

    BookingRequest request =
            new BookingRequest("C001", "E001", quantity, price);

    when(paymentGateway.processPayment("C001", expectedTotal))
            .thenReturn(Optional.of("TXN001"));

    BookingResult result = bookingManager.book(request);

    assertEquals(BookingResult.Status.SUCCESS, result.status());
    assertEquals("TXN001", result.transactionId());

    Booking expectedBooking =
            new Booking(request, expectedTotal, "TXN001");

    verify(eventRepository).isSoldOut("E001");
    verify(paymentGateway).processPayment("C001", expectedTotal);
    verify(eventRepository).saveBooking(expectedBooking);
    verify(notificationService).sendConfirmation(expectedBooking);
}

static Stream<Arguments> validBoundaryRequests() {
    return Stream.of(
            Arguments.of(
                    "Minimum quantity",
                    1, 1500, 1500L
            ),
            Arguments.of(
                    "Maximum quantity",
                    5, 1500, 7500L
            ),
            Arguments.of(
                    "Large price without integer overflow",
                    5, Integer.MAX_VALUE, 10737418235L
            )
    );
}

@ParameterizedTest
@org.junit.jupiter.params.provider.ValueSource(
        strings = {"", "   "}
)
void invalidTransactionIdShouldNotConfirmBooking(
        String transactionId) {

    BookingRequest request =
            new BookingRequest("C001", "E001", 2, 1500);

    when(paymentGateway.processPayment("C001", 3000L))
            .thenReturn(Optional.of(transactionId));

    BookingResult result = bookingManager.book(request);

    assertEquals(
            BookingResult.Status.PAYMENT_FAILED,
            result.status()
    );

    assertEquals(null, result.transactionId());

    verify(eventRepository).isSoldOut("E001");
    verify(paymentGateway).processPayment("C001", 3000L);
    verify(eventRepository, never()).saveBooking(any());
    verifyNoInteractions(notificationService);
}


}

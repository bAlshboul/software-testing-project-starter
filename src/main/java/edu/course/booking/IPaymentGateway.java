package edu.course.booking;

import java.util.Optional;

public interface IPaymentGateway {
    /** Returns a nonblank transaction ID on success or an empty result when payment fails. */
    Optional<String> processPayment(String customerId, long amountInCents);
}

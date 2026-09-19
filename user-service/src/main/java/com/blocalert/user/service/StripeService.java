package com.blocalert.user.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Event;

public interface StripeService {

    void handleStripeCallback(Event event) throws StripeException;
}

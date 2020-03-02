package org.psc.playground.reactor;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class OrderIdProvider {

    private static final AtomicLong CURRENT_ORDER_ID = new AtomicLong(0L);

    public long next() {
        return CURRENT_ORDER_ID.incrementAndGet();
    }
}

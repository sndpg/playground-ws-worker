package org.psc.playground.reactor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@With
public class OrderMessage {

    private long messageIndex;
    private String origin;
    private LocalDateTime created = LocalDateTime.now();
    private OrderAction action;
    private OrderStatus status;
    private long customerId;
    private long orderId;
    private List<OrderDetail> orderDetails;
    private LocalDateTime orderedAt;

    public static OrderMessage nextFromIncoming(OrderMessage orderMessage) {
        return new OrderMessage().withMessageIndex(orderMessage.getMessageIndex() + 1)
                .withCustomerId(orderMessage.getCustomerId())
                .withOrderDetails(orderMessage.getOrderDetails())
                .withOrderedAt(orderMessage.getOrderedAt())
                .withOrigin("OrderServer");
    }

}

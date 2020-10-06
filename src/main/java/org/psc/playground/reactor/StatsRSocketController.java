package org.psc.playground.reactor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StatsRSocketController {

    private final OrderIdProvider orderIdProvider;

    private final FluxProcessor<OrderMessage, OrderMessage> incomingOrders = DirectProcessor.create();

    @MessageMapping("orders")
    public OrderMessage orderMessageExchange(OrderMessage request) {
        incomingOrders.onNext(request);
        // do something with request
        // ...
        OrderMessage response = OrderMessage.nextFromIncoming(request);

        switch (request.getAction()) {
            case SUBMIT -> {
                response.setOrderId(orderIdProvider.next());
                response.setOrderedAt(LocalDateTime.now());
                response.setStatus(OrderStatus.RECEIVED);
            }
            case CANCEL -> {
                response.setOrderId(request.getOrderId());
                response.setStatus(OrderStatus.CANCELLED);

            }
            case CHANGE -> {
                response.setOrderId(request.getOrderId());
                response.setStatus(OrderStatus.RECEIVED);
                log.info("change something");
            }
            default -> response.setOrderId(request.getOrderId());
        }

        response.setCreated(LocalDateTime.now());
        return response;
    }

    @MessageMapping("incoming-orders-stats")
    public Flux<OrdersStatistics> incomingOrdersStats() {
        return incomingOrders.filter(orderMessage -> orderMessage.getAction() == OrderAction.SUBMIT)
                .scan(new OrdersStatistics(), (stats, currentOrderMessage) -> {
                    stats.setCount(stats.getCount() + 1);
                    stats.setTotalSum(stats.getTotalSum()
                            .add(currentOrderMessage.getOrderDetails()
                                    .stream()
                                    .map(orderDetail -> orderDetail.getPrice()
                                            .multiply(BigDecimal.valueOf(orderDetail.getItemCount())))
                                    .reduce(
                                            BigDecimal::add)
                                    .orElse(BigDecimal.ZERO)));
                    stats.setAverageOrderTotal(
                            stats.getTotalSum().divide(BigDecimal.valueOf(stats.getCount()), RoundingMode.HALF_UP));
                    return stats;
                })
                .publish()
                .autoConnect();
    }
}

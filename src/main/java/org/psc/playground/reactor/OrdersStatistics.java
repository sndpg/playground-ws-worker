package org.psc.playground.reactor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@With
public class OrdersStatistics {
    private long count;
    private BigDecimal totalSum = BigDecimal.ZERO;
    private BigDecimal averageOrderTotal = BigDecimal.ZERO;
}

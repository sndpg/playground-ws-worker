package org.psc.playground.reactor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@With
public class OrderDetail {
    private long productId;
    private BigDecimal price;
    private int itemCount;
}

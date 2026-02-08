package com.example.instructions.model;

import java.math.BigDecimal;
import lombok.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformTrade {
    private String platformId;
    private Trade trade;

    @Data
    @Builder
    public static class Trade {
        private String account;
        private String security;
        private String tradeType;            // BUY/SELL
        private BigDecimal amount;
        private Instant timestamp;
    }

}

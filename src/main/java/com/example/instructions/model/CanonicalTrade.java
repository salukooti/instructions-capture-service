package com.example.instructions.model;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"accountNumber"})
public class CanonicalTrade {
    private String tradeId;
    private String accountNumber;   // need to mask when stored/returned
    private String securityId;
    private String tradeType;       // B / S / SS / C
    private BigDecimal price;
    private Instant tradeTS;
}

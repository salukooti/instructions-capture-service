package com.example.instructions.util;

import com.example.instructions.model.CanonicalTrade;
import com.example.instructions.model.InputTrade;
import com.example.instructions.model.PlatformTrade;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class TradeTransformer {
    private TradeTransformer() {
    }

    private static final Map<String, String> TRADE_TYPE_MAP = Map.ofEntries(
            Map.entry("BUY", "B"),
            Map.entry("B", "B"),
            Map.entry("SELL", "S"),
            Map.entry("S", "S")
    );

    /**
     * Convert instruction into canonical format (normalize + mask).
     */
    public static CanonicalTrade toCanonical(InputTrade in) {
        final String id = UUID.randomUUID().toString();

        final String platformId = (in.getPlatformId() == null || in.getPlatformId().isBlank())
                ? "ACCT123"
                : sanitize(in.getPlatformId());

        final String maskedAccount = maskAccount(in.getAccountNumber());
        final String security = sanitize(in.getSecurityId()).toUpperCase(Locale.ROOT);
        validateSecurityId(security);

        final String typeCode = validateTradeType(in.getTradeType());
        final Instant ts = parseInstant(in.getTimestamp());
        return CanonicalTrade.builder()
                .tradeId(id)
                .accountNumber(maskedAccount)
                .securityId(security)
                .tradeType(typeCode)
                .price(in.getAmount())
                .tradeTS(ts)
                .build();
    }

    /**
     * Convert canonical record to platform JSON structure.
     */
    public static PlatformTrade toPlatformTrade(CanonicalTrade c) {
        /**
         private String tradeId;
         private String accountNumber;   // need to mask when send it to outbound
         private String securityId;
         private String tradeType;       // BUY/B / SELL/S
         private BigDecimal price;
         private Instant tradeTS;
         */
        return PlatformTrade.builder()
                .platformId(c.getTradeId())
                .trade(PlatformTrade.Trade.builder()
                        .account(c.getAccountNumber())
                        .security(c.getSecurityId())
                        .tradeType(c.getTradeType())
                        .amount(c.getPrice())
                        .timestamp(c.getTradeTS())
                        .build())
                .build();
    }
    public static PlatformTrade toPlatformTrade(InputTrade c) {
        final String id = UUID.randomUUID().toString();
        /**
         private String platformId;
         private String accountNumber;
         private String securityId;
         private String tradeType;
         private BigDecimal amount;
         private String timestamp;
         */
        return PlatformTrade.builder()
                .platformId(id)
                .trade(PlatformTrade.Trade.builder()
                        .account(c.getAccountNumber())
                        .security(c.getSecurityId())
                        .tradeType(c.getTradeType())
                        .amount(c.getAmount())
                        //.timestamp(c.getTimestamp())
                        .build())
                .build();
    }

    public static String maskAccount(String digits) {
        final String sanitized = sanitize(digits);
        if (!sanitized.matches("^\\d{4,32}$")) {
            throw new IllegalArgumentException("account_number must be 4-32 digits");
        }
        int len = sanitized.length();
        String last4 = sanitized.substring(len - 4);
        return "*".repeat(len - 4) + last4;
    }

    public static String validateTradeType(String raw) {
        String key = sanitize(raw).toUpperCase(Locale.ROOT);
        String mapped = TRADE_TYPE_MAP.get(key);
        if (mapped == null) {
            mapped = TRADE_TYPE_MAP.get(key.replaceAll("\\s+", ""));
        }
        if (mapped == null) {
            throw new IllegalArgumentException("Unsupported trade_type: " + key);
        }
        return mapped;
    }

    public static void validateSecurityId(String security) {
        //3-20 uppercase alphanumeric
        if (!security.matches("^[A-Z0-9]{3,20}$")) {
            throw new IllegalArgumentException("Invalid security_id format");
        }
    }

    public static Instant parseInstant(String raw) {
        try {
            return Instant.parse(sanitize(raw));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "timestamp must be Instant (e.g., 2025-08-04T21:15:33Z)");
        }
    }

    /**
     * String sanitization to reduce injection/CRLF issues, We can use OWASP API too.
     */
    public static String sanitize(String s) {
        if (s == null) return "";
        return s.replaceAll("[\\r\\n\\t]", "").trim();
    }
}


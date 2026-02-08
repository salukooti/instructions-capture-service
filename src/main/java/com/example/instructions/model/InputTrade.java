package com.example.instructions.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InputTrade {

    //@JsonProperty("platform_id")
    @JsonAlias({"platformId","platform_id"})
    private String platformId;

    //@JsonProperty("account_number")
    @JsonAlias({"accountNumber","account_number"})
    @NotBlank
    @Pattern(regexp = "^[0-9]{4,32}$", message = "account_number must be 4-32 digits")
    private String accountNumber;

    //@JsonProperty("security_id")
    @JsonAlias({"security","security_id"})
    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9]{3,20}$", message = "security_id must be 3-20 alphanumeric chars")
    private String securityId;

    //@JsonProperty("trade_type")
    @JsonAlias({"tradeType","trade_type"})
    @NotBlank
    private String tradeType;

    @JsonProperty("amount")
    @NotNull
    private BigDecimal amount;

    @JsonProperty("timestamp")
    //@JsonFormat(shape = JsonFormat.Shape.STRING)
    @NotBlank
    private String timestamp;

}

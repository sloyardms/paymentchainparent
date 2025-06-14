package com.paymentchain.billing.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
@Schema(name = "Invoice", description = "This model represents an invoice")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Schema(name = "customerId", requiredMode = Schema.RequiredMode.REQUIRED, example = "1", description = "Unique Id of customer that represent the owner of the invoice")
    private long customerId;

    @Schema(name = "number", requiredMode = Schema.RequiredMode.REQUIRED, example = "3", description = "Number given on physical invoice")
    private String number;

    private String detail;
    private double amount;

}

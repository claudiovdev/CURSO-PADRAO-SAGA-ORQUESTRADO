package br.com.microservices.orchestrated.orchestratorservice.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ETopics {

    START_SAGA("start-saga"),
    BASE_ORCHESTRATOR("orchestrator"),
    FINISH_SUCCESS("finish-success"),
    FINISH_FAIL("finish-fail"),
    PRODUCT_VALIDATION_SUCCESS("product-validation-success"),
    PRODUCT_VALIDATION_FAILT("product-validation-fail"),
    PAYMENT_SUCCESS("payment-success"),
    PAYMENT_SUCCESS_FAILT("payment-fail"),
    INVENTORY_SUCCESS("inventory-success"),
    INVENTORY_SUCCESS_FAILT("inventory-fail"),
    NOTIFY_ENDING("notify-ending");

    private String topic;
}

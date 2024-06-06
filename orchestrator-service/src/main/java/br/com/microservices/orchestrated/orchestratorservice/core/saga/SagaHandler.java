package br.com.microservices.orchestrated.orchestratorservice.core.saga;

import static br.com.microservices.orchestrated.orchestratorservice.core.enums.EEventSource.*;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ESagaStatus.*;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics.*;

public final class SagaHandler {

    public SagaHandler() {
    }

    public static final Object[][] SAGA_HANDLER = {
            { ORCHESTRATOR,SUCCESS,PRODUCT_VALIDATION_SUCCESS },
            { ORCHESTRATOR,FAIL, PRODUCT_VALIDATION_FAIL},

            { PRODUCT_VALIDATION_SERVICE,ROLLBACK_PENDING, PRODUCT_VALIDATION_FAIL},
            { PRODUCT_VALIDATION_SERVICE,FAIL, FINISH_FAIL},
            { PRODUCT_VALIDATION_SERVICE,SUCCESS, PAYMENT_SUCCESS},

            { PAYMENT_SERVICE,ROLLBACK_PENDING, PAYMENT_SUCCESS_FAIL},
            { PAYMENT_SERVICE,FAIL, PRODUCT_VALIDATION_FAIL},
            { PAYMENT_SERVICE,SUCCESS, INVENTORY_SUCCESS},

            { INVENTORY_SUCCESS,ROLLBACK_PENDING, INVENTORY_SUCCESS_FAIL},
            { PAYMENT_SERVICE,FAIL, PAYMENT_SUCCESS_FAIL},
            { PAYMENT_SERVICE,SUCCESS, FINISH_SUCCESS},


    };

    public static final int EVENT_SOURCE_INDEX = 0;
    public static final int SAGA_STATUS_INDEX = 1;
    public static final int TOPIC_INDEX = 2;
}
package br.com.microservices.orchestrated.paymentservice.core.service;

import br.com.microservices.orchestrated.paymentservice.config.execption.ValidationException;
import br.com.microservices.orchestrated.paymentservice.core.dto.Event;
import br.com.microservices.orchestrated.paymentservice.core.dto.History;
import br.com.microservices.orchestrated.paymentservice.core.dto.OrderProducts;
import br.com.microservices.orchestrated.paymentservice.core.enums.EPaymentStatus;
import br.com.microservices.orchestrated.paymentservice.core.enums.ESagaStatus;
import br.com.microservices.orchestrated.paymentservice.core.model.Payment;
import br.com.microservices.orchestrated.paymentservice.core.producer.KafkaProducer;
import br.com.microservices.orchestrated.paymentservice.core.repository.PaymentRepository;
import br.com.microservices.orchestrated.paymentservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@AllArgsConstructor
public class PaymentService {
    private static final String CURRENT_SOURCE = "PAYMENT_SERVICE";
    private static final Double REDUCE_SUM_VALUE = 0.0;

    private static final Double MIN_AMOUNT_VALUE = 0.1;

    private final JsonUtil jsonUtil;
    private final KafkaProducer kafkaProducer;
    private final PaymentRepository paymentRepository;

    public void realizePayment(Event event){
        try {
            checkCurrentValidation(event);
            createPendingPayment(event);
            var payment = findByOrderIdAndTransactionId(event);
            validateAmount(payment.getTotalAmount());
            changedPaymentToSuccess(payment);
            handleSuccess(event);
        }catch (Exception ex){
            log.error("Error trying to make validate payment: ", ex);
            handleFailCurrentNotExecuted(event,ex.getMessage());
        }
        kafkaProducer.sendEvent(jsonUtil.toJson(event));

    }



    private void checkCurrentValidation(Event event) {
        if (paymentRepository.existsByOrderIdAndTransactionId(event.getOrderId(), event.getTransactionId())){
            throw new ValidationException("there's another transactionId for this validation");
        }
    }

    private void createPendingPayment(Event event) {
        var totalAmount = calculateAmount(event);
        var totalItens =   calculateTotalItems(event);

        var payment = Payment
                .builder()
                .orderId(event.getPayload().getId())
                .transactionId(event.getTransactionId())
                .totalAmount(totalAmount)
                .totalItems(totalItens)
                .build();
        save(payment);
        setEventAmountItens(event, payment);
    }

    private Payment findByOrderIdAndTransactionId(Event event){
        return paymentRepository.findByOrderIdAndTransactionId(event.getPayload().getId(), event.getTransactionId())
                .orElseThrow(()-> new ValidationException("Payment not found by orderId and transactionId"));
    }

    private void save(Payment payment){
        paymentRepository.save(payment);
    }

    private double calculateAmount(Event event){
        return event
                .getPayload()
                .getProducts()
                .stream()
                .map(product -> product.getQuantity() * product.getProduct().getUnitValue())
                .reduce(REDUCE_SUM_VALUE, Double::sum);
    }

    private void validateAmount(double totalAmount){
        if (totalAmount < MIN_AMOUNT_VALUE){
            throw new ValidationException("The minimum amount available is ".concat(MIN_AMOUNT_VALUE.toString()));
        }
    }

    private void changedPaymentToSuccess(Payment payment){
        payment.setStatus(EPaymentStatus.SUCCESS);
        save(payment);
    }

    private int calculateTotalItems(Event event){
        return event
                .getPayload()
                .getProducts()
                .stream()
                .map(OrderProducts::getQuantity)
                .reduce(REDUCE_SUM_VALUE.intValue(), Integer::sum);
    }
    private void handleSuccess(Event event) {
        event.setStatus(ESagaStatus.SUCCESS);
        event.setSource(CURRENT_SOURCE);
        addHistory(event, "Payment realized with successfully!");

    }

    public void realizeRefound(Event event){
        event.setStatus(ESagaStatus.FAIL);
        event.setSource(CURRENT_SOURCE);
        try{
            changePaymentStatusToRefond(event);
            addHistory(event, "Rollback executed for payment!");
        }catch (Exception ex){
            addHistory(event, "Rollback not executed for payment: ".concat(ex.getMessage()) );
        }
        changePaymentStatusToRefond(event);
        kafkaProducer.sendEvent(jsonUtil.toJson(event));
    }

    private void changePaymentStatusToRefond(Event event){
        var payment = findByOrderIdAndTransactionId(event);
        payment.setStatus(EPaymentStatus.REFOUND);
        setEventAmountItens(event, payment);
        save(payment);
    }
    private void handleFailCurrentNotExecuted(Event event, String message){
        event.setStatus(ESagaStatus.ROLLBACK_PENDING);
        event.setSource(CURRENT_SOURCE);
        addHistory(event, "Fail to realize  payment: ".concat(message));
    }

    private void setEventAmountItens(Event event, Payment payment){
        event.getPayload().setTotalAmount(payment.getTotalAmount());
        event.getPayload().setTotalItems(payment.getTotalItems());
    }

    private void addHistory(Event event, String message){
        var history = History
                .builder()
                .source(CURRENT_SOURCE)
                .status(ESagaStatus.SUCCESS)
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();
        event.addToHistory(history);
    }
}

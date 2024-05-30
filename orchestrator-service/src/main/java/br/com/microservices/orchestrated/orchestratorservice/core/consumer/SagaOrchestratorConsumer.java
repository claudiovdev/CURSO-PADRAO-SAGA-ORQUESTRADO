package br.com.microservices.orchestrated.orchestratorservice.core.consumer;

import br.com.microservices.orchestrated.orchestratorservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class SagaOrchestratorConsumer {

    private final JsonUtil jsonUtil;

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.start-saga}"
    )
    public void consumerStartSagaEvent(String paylad){
        log.info("Receiving event {} from start-saga topic", paylad);
        var event = jsonUtil.toEvent(paylad);
        log.info(event.toString());
    }

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.orchestrator}"
    )
    public void consumerOrchestratorEvent(String paylad){
        log.info("Receiving event {} from orchestrator topic", paylad);
        var event = jsonUtil.toEvent(paylad);
        log.info(event.toString());
    }

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.finish-success}"
    )
    public void consumerFinishSuccessEvent(String paylad){
        log.info("Receiving event {} from finish-success topic", paylad);
        var event = jsonUtil.toEvent(paylad);
        log.info(event.toString());
    }

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.finish-fail}"
    )
    public void consumerFinishFailEvent(String paylad){
        log.info("Receiving event {} from finish-fail topic", paylad);
        var event = jsonUtil.toEvent(paylad);
        log.info(event.toString());
    }
}

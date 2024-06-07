package br.com.microservices.orchestrated.orchestratorservice.core.service;

import br.com.microservices.orchestrated.orchestratorservice.core.dto.Event;
import br.com.microservices.orchestrated.orchestratorservice.core.dto.History;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.EEventSource;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.ESagaStatus;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics;
import br.com.microservices.orchestrated.orchestratorservice.core.producer.SagaOrchestratorProducer;
import br.com.microservices.orchestrated.orchestratorservice.core.saga.SagaExecutionController;
import br.com.microservices.orchestrated.orchestratorservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static br.com.microservices.orchestrated.orchestratorservice.core.enums.EEventSource.ORCHESTRATOR;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ESagaStatus.FAIL;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ESagaStatus.SUCCESS;

@Slf4j
@Service
@AllArgsConstructor
public class OrchestratorService {

    private final JsonUtil jsonUtil;
    private final SagaOrchestratorProducer producer;
    private final SagaExecutionController sagaExecutionController;

    public void startSaga(Event event){
        event.setSource(ORCHESTRATOR);
        event.setStatus(SUCCESS);
        var topic = getTopics(event);
        log.info("SAGA STARTED!");
        addHistory(event, "Saga Started !");
        sendToProducerWithTopic(event, topic);

    }

    public void finishSagaSuccess(Event event){
        event.setSource(ORCHESTRATOR);
        event.setStatus(SUCCESS);
        log.info("SAGA FINISHED SUCCESSFULY FOR EVENT {}!", event.getId());
        addHistory(event, "Saga finished successfully!");
        notifyFinishedSaga(event);

    }

    public void finishSagaFail(Event event){
        event.setSource(ORCHESTRATOR);
        event.setStatus(FAIL);
        log.info("SAGA FINISHED WITH ERRORS FOR EVENT {}!", event.getId());
        addHistory(event, "Saga finished with errors!");
        notifyFinishedSaga(event);
    }

    public void continueSaga(Event event){
        var topic = getTopics(event);
        log.info("SAGA CONTINUE FOR EVENT {}", event.getId());
        sendToProducerWithTopic(event, topic);
    }

    private ETopics getTopics(Event event){
        return sagaExecutionController.getNextTopic(event);
    }

    private void addHistory(Event event, String message){
        var history = History
                .builder()
                .source(event.getSource())
                .status(ESagaStatus.SUCCESS)
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();
        event.addToHistory(history);
    }

    private void notifyFinishedSaga(Event event){
        producer.sendEvent(jsonUtil.toJson(event), ETopics.NOTIFY_ENDING.getTopic());
    }

    private void sendToProducerWithTopic(Event event, ETopics topic){
        producer.sendEvent(jsonUtil.toJson(event), topic.getTopic());
    }

}

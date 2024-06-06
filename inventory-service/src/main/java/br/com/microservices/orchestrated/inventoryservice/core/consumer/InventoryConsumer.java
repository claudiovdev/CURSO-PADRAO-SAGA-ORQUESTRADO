package br.com.microservices.orchestrated.inventoryservice.core.consumer;

import br.com.microservices.orchestrated.inventoryservice.core.service.InventoryService;
import br.com.microservices.orchestrated.inventoryservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class InventoryConsumer {

    private final JsonUtil jsonUtil;
    private final InventoryService inventoryService;

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.inventory-success}"
    )
    public void consumerSuccessEvent(String paylad){
        log.info("Receiving success event {} from inventory-success topic", paylad);
        var event = jsonUtil.toEvent(paylad);
        log.info(event.toString());
        inventoryService.updateInventory(event);
    }

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.inventory-fail}"
    )
    public void consumerFailEvent(String paylad){
        log.info("Receiving rollback event {} from inventory-fail topic", paylad);
        var event = jsonUtil.toEvent(paylad);
        log.info(event.toString());
        inventoryService.rollbackInventory(event);
    }


}

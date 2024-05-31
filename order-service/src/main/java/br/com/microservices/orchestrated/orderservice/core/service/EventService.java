package br.com.microservices.orchestrated.orderservice.core.service;

import br.com.microservices.orchestrated.orderservice.config.exception.ValidationException;
import br.com.microservices.orchestrated.orderservice.core.document.Event;
import br.com.microservices.orchestrated.orderservice.core.dto.EventFilter;
import br.com.microservices.orchestrated.orderservice.core.repository.EventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
@AllArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public void notifyEnding(Event event){
        event.setOrderId(event.getOrderId());
        event.setCreatedAt(LocalDateTime.now());
        save(event);
        log.info("Order {} with saga notified! transactionId: {}", event.getOrderId(), event.getTransactionId());
    }

    public List<Event> findAll(){
        return  eventRepository.findAllByOrderByCreatedAtDesc();
    }

    public Event findByFilters(EventFilter eventFilter){
        validateEmptyFilters(eventFilter);
        if(!isEmpty(eventFilter.getOrderId())){
            return findByOrderId(eventFilter.getOrderId());
        }else {
            return findByTransactionId(eventFilter.getTransactionId());
        }
    }


    public Event save(Event event){
        return eventRepository.save(event);
    }

    private void validateEmptyFilters(EventFilter eventFilter){
        if(isEmpty(eventFilter.getOrderId()) && isEmpty(eventFilter.getTransactionId())){
            throw new ValidationException("OrderId or TransactionId must be informed.");
        }
    }

    private Event findByOrderId(String orderId){
        return eventRepository.findTop1ByOrderIdOrderByCreatedAtDesc(orderId)
                .orElseThrow(() -> new ValidationException("Event not found by orderId."));
    }

    private Event findByTransactionId(String transactionId){
        return eventRepository.findTop1ByTransactionIdOrderByCreatedAtDesc(transactionId)
                .orElseThrow(() -> new ValidationException("Event not found by transactionId."));
    }
}

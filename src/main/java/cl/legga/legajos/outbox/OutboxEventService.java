package cl.legga.legajos.outbox;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;

    public OutboxEventService(
            OutboxEventRepository outboxEventRepository) {
        this.outboxEventRepository = outboxEventRepository;
    }

    public void registrarEvento(
            String eventType,
            String aggregateType,
            UUID aggregateId,
            String payloadJson) {

        OutboxEvent event = new OutboxEvent(
                eventType,
                aggregateType,
                aggregateId,
                payloadJson);

        outboxEventRepository.save(event);
    }
}
package cl.legga.legajos.outbox;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OutboxEventService {

    private static final int MAX_INTENTOS = 5;

    private final OutboxEventRepository outboxEventRepository;

    public OutboxEventService(
            OutboxEventRepository outboxEventRepository) {
        this.outboxEventRepository = outboxEventRepository;
    }

    @Transactional
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

    @Transactional
    public List<OutboxEvent> reclamarEventosPendientes(int limite) {
        if (limite <= 0) {
            throw new IllegalArgumentException(
                    "El límite debe ser mayor que cero");
        }

        List<OutboxEvent> lote =
                outboxEventRepository.reclamarPendientes("PENDING", limite);

        lote.forEach(OutboxEvent::marcarEnProceso);

        return outboxEventRepository.saveAll(lote);
    }

    @Transactional
    public void marcarPublicado(UUID eventId) {
        OutboxEvent event = obtenerEvento(eventId);
        event.marcarPublicado();
        outboxEventRepository.save(event);
    }

    @Transactional
    public void registrarFallo(UUID eventId, String error) {
        OutboxEvent event = obtenerEvento(eventId);
        event.registrarError(error, MAX_INTENTOS);
        outboxEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<OutboxEvent> obtenerEventosPendientes() {
        return outboxEventRepository.findAll()
                .stream()
                .filter(event -> "PENDING".equals(event.getStatus()))
                .toList();
    }

    private OutboxEvent obtenerEvento(UUID eventId) {
        return outboxEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe el evento Outbox: " + eventId));
    }
}

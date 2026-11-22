package cl.legga.legajos.outbox;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxPublisher {

    private static final int LIMITE_LOTE = 50;

    private final OutboxEventService outboxEventService;
    private final EventGridPublisherClient<EventGridEvent> eventGridClient;

    public OutboxPublisher(
            OutboxEventService outboxEventService,
            @Value("${azure.eventgrid.endpoint}") String endpoint,
            @Value("${azure.eventgrid.key}") String key) {

        this.outboxEventService = outboxEventService;

        this.eventGridClient = new EventGridPublisherClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(key))
                .buildEventGridEventPublisherClient();
    }

    @Scheduled(fixedDelayString = "${outbox.publisher.delay-ms:60000}")
    public void publicarPendientes() {

        List<OutboxEvent> eventos = outboxEventService.reclamarEventosPendientes(LIMITE_LOTE);

        for (OutboxEvent evento : eventos) {
            try {
                EventGridEvent eventGridEvent = new EventGridEvent(
                        "/legga/legajos/" + evento.getAggregateId(),
                        evento.getEventType(),
                        BinaryData.fromString(evento.getPayload()),
                        "1.0");

                eventGridClient.sendEvent(eventGridEvent);

                outboxEventService.marcarPublicado(evento.getEventId());

            } catch (Exception ex) {
                outboxEventService.registrarFallo(
                        evento.getEventId(),
                        ex.getMessage() != null
                                ? ex.getMessage()
                                : "Error desconocido al publicar");
            }
        }
    }
}
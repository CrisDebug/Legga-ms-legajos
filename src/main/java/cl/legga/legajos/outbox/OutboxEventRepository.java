
package cl.legga.legajos.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

        @Query(value = """
                        SELECT *
                        FROM legajos.outbox_event
                        WHERE status = :status
                        ORDER BY occurred_at ASC
                        LIMIT :limite
                        FOR UPDATE SKIP LOCKED
                        """, nativeQuery = true)
        List<OutboxEvent> reclamarPendientes(
                        @Param("status") String status,
                        @Param("limite") int limite);
}

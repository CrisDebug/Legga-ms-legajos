
package cl.legga.bff.categorias.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CategoriaCampoResponseDTO(
        UUID id,
        UUID categoriaId,
        String codigo,
        String etiqueta,
        String tipoDato,
        Boolean obligatorio,
        Integer orden,
        Boolean activo,
        JsonNode opciones,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}

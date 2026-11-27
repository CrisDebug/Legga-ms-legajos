
package cl.legga.bff.categorias.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record CategoriaCampoRequestDTO(
        String codigo,
        String etiqueta,
        String tipoDato,
        Boolean obligatorio,
        Integer orden,
        JsonNode opciones) {
}

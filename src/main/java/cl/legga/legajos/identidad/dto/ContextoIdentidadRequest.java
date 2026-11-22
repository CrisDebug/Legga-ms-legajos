
package cl.legga.legajos.identidad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para consultar el contexto de identidad.
 *
 * Recibe los identificadores provenientes de Microsoft Entra ID,
 * obtenidos previamente por el BFF mediante Easy Auth.
 *
 * Estos datos permiten localizar al usuario en PostgreSQL
 * utilizando la combinación tenantId + objectId.
 */
public class ContextoIdentidadRequest {

    /**
     * Identificador del tenant de Microsoft Entra ID.
     */
    @NotBlank(message = "El tenantId es obligatorio")
    @Size(max = 100, message = "El tenantId no puede superar los 100 caracteres")
    private String tenantId;

    /**
     * Identificador único del usuario dentro del tenant.
     */
    @NotBlank(message = "El objectId es obligatorio")
    @Size(max = 100, message = "El objectId no puede superar los 100 caracteres")
    private String objectId;

    /**
     * Constructor vacío requerido para la deserialización JSON.
     */
    public ContextoIdentidadRequest() {
    }

    /**
     * Constructor con todos los atributos.
     */
    public ContextoIdentidadRequest(String tenantId, String objectId) {
        this.tenantId = tenantId;
        this.objectId = objectId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }
}

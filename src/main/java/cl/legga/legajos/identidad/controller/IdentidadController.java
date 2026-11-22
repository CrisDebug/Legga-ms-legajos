
package cl.legga.legajos.identidad.controller;

import cl.legga.legajos.identidad.dto.ContextoIdentidadRequest;
import cl.legga.legajos.identidad.dto.ContextoIdentidadResponse;
import cl.legga.legajos.identidad.service.IdentidadService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST del módulo de identidad de LEGGA.
 *
 * Expone operaciones para consultar el contexto de identidad
 * y los permisos funcionales almacenados en PostgreSQL.
 *
 * Este endpoint está destinado a la comunicación interna
 * entre el BFF y MS-Legajos.
 */
@RestController
@RequestMapping("/api/identidad")
public class IdentidadController {

    private final IdentidadService identidadService;

    public IdentidadController(IdentidadService identidadService) {
        this.identidadService = identidadService;
    }

    /**
     * Consulta el contexto del usuario autenticado.
     *
     * Recibe tenantId y objectId proporcionados por el BFF,
     * después de validar la identidad mediante Easy Auth.
     *
     * @param request identificadores de Microsoft Entra ID
     * @return contexto del usuario, institución, perfil y permisos
     */
    @PostMapping("/contexto")
    public ContextoIdentidadResponse consultarContexto(
            @Valid @RequestBody ContextoIdentidadRequest request) {

        return identidadService.consultarContexto(request);
    }
}

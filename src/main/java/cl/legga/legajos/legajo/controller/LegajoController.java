
package cl.legga.legajos.legajo.controller;

import cl.legga.legajos.legajo.entity.Legajo;
import cl.legga.legajos.legajo.dto.LegajoResponseDTO;
import cl.legga.legajos.legajo.service.LegajoService;
import cl.legga.legajos.identidad.dto.ContextoIdentidadRequest;
import cl.legga.legajos.identidad.dto.ContextoIdentidadResponse;
import cl.legga.legajos.identidad.service.IdentidadService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/legajos")
public class LegajoController {

        private final LegajoService legajoService;
        private final IdentidadService identidadService;

        public LegajoController(
                        LegajoService legajoService,
                        IdentidadService identidadService) {
                this.legajoService = legajoService;
                this.identidadService = identidadService;
        }

        // =========================================================
        // CONSULTAR LEGAJOS
        // =========================================================

        /**
         * Lista los legajos de la institución autenticada.
         * La respuesta utiliza DTO para exponer los datos necesarios
         * sin entregar directamente las entidades JPA.
         */
        @GetMapping
        public cl.legga.legajos.legajo.dto.LegajoPageResponseDTO listar(
                        @RequestParam(required = false) String estado,
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false) UUID categoriaId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "6") int size,
                        @RequestHeader("X-LEGGA-TENANT-ID") String tenantId,
                        @RequestHeader("X-LEGGA-OBJECT-ID") String objectId) {

                var contexto = obtenerContexto(tenantId, objectId);
                validarPermiso(contexto, "F-01-CONSULTAR");

                var resultado = legajoService.buscarPaginadoPorInstitucion(
                                contexto.getInstitucionId(),
                                q,
                                categoriaId,
                                estado,
                                page,
                                size);

                return new cl.legga.legajos.legajo.dto.LegajoPageResponseDTO(resultado);
        }

        /**
         * Consulta el detalle de un legajo.
         *
         * IMPORTANTE:
         * Se utiliza buscarPorIdEInstitucionDTO() para devolver
         * categoriaId y categoriaNombre en la respuesta.
         */
        @GetMapping("/{id}")
        public LegajoResponseDTO buscarPorId(
                        @PathVariable UUID id,
                        @RequestHeader("X-LEGGA-TENANT-ID") String tenantId,
                        @RequestHeader("X-LEGGA-OBJECT-ID") String objectId) {

                var contexto = obtenerContexto(tenantId, objectId);
                validarPermiso(contexto, "F-01-CONSULTAR");

                return legajoService.buscarPorIdEInstitucionDTO(
                                id,
                                contexto.getInstitucionId());
        }

        /**
         * Busca legajos por título o categoría.
         */
        @GetMapping("/buscar")
        public List<Legajo> buscar(
                        @RequestParam(required = false) String titulo,
                        @RequestParam(required = false) UUID categoriaId,
                        @RequestHeader("X-LEGGA-TENANT-ID") String tenantId,
                        @RequestHeader("X-LEGGA-OBJECT-ID") String objectId) {

                if ((titulo == null || titulo.isBlank()) && categoriaId == null) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Debe indicar un título o una categoría para buscar");
                }

                var contexto = obtenerContexto(tenantId, objectId);
                validarPermiso(contexto, "F-01-CONSULTAR");

                return legajoService.buscarPorInstitucion(
                                titulo,
                                categoriaId,
                                contexto.getInstitucionId());
        }

        // =========================================================
        // CREAR LEGAJO
        // =========================================================

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public Legajo crear(
                        @RequestBody Legajo legajo,
                        @RequestParam UUID categoriaId,
                        @RequestHeader("X-LEGGA-TENANT-ID") String tenantId,
                        @RequestHeader("X-LEGGA-OBJECT-ID") String objectId) {

                var contexto = obtenerContexto(tenantId, objectId);
                validarPermiso(contexto, "F-01-CREAR");

                // El creador se obtiene del contexto de identidad.
                // No se confía en un valor enviado desde el formulario.
                legajo.setCreadoPor(contexto.getCorreo());

                return legajoService.crear(
                                legajo,
                                categoriaId,
                                contexto.getInstitucionId());
        }

        // =========================================================
        // EDICION COMPLETA - PUT
        // =========================================================

        @PutMapping("/{id}")
        public Legajo reemplazar(
                        @PathVariable UUID id,
                        @RequestParam UUID categoriaId,
                        @RequestBody Legajo datos,
                        @RequestHeader("X-LEGGA-TENANT-ID") String tenantId,
                        @RequestHeader("X-LEGGA-OBJECT-ID") String objectId) {

                var contexto = obtenerContexto(tenantId, objectId);
                validarPermiso(contexto, "F-01-EDITAR");

                return legajoService.reemplazar(
                                id,
                                contexto.getInstitucionId(),
                                datos.getTitulo(),
                                datos.getDescripcion(),
                                categoriaId,
                                contexto.getCorreo());
        }

        // =========================================================
        // EDICION PARCIAL - PATCH
        // =========================================================

        @PatchMapping("/{id}")
        public Legajo actualizarParcialmente(
                        @PathVariable UUID id,
                        @RequestBody Map<String, Object> cambios,
                        @RequestHeader("X-LEGGA-TENANT-ID") String tenantId,
                        @RequestHeader("X-LEGGA-OBJECT-ID") String objectId) {

                var contexto = obtenerContexto(tenantId, objectId);
                validarPermiso(contexto, "F-01-EDITAR");

                return legajoService.actualizarParcialmente(
                                id,
                                contexto.getInstitucionId(),
                                cambios,
                                contexto.getCorreo());
        }

        // =========================================================
        // ARCHIVAR LEGAJO
        // =========================================================

        @PatchMapping("/{id}/archivar")
        public Legajo archivar(
                        @PathVariable UUID id,
                        @RequestHeader("X-LEGGA-TENANT-ID") String tenantId,
                        @RequestHeader("X-LEGGA-OBJECT-ID") String objectId) {

                var contexto = obtenerContexto(tenantId, objectId);
                validarPermiso(contexto, "F-02-ARCHIVAR");

                return legajoService.archivar(
                                id,
                                contexto.getInstitucionId(),
                                contexto.getCorreo());
        }

        // =========================================================
        // REACTIVAR LEGAJO
        // =========================================================

        @PatchMapping("/{id}/reactivar")
        public Legajo reactivar(
                        @PathVariable UUID id,
                        @RequestHeader("X-LEGGA-TENANT-ID") String tenantId,
                        @RequestHeader("X-LEGGA-OBJECT-ID") String objectId) {

                var contexto = obtenerContexto(tenantId, objectId);
                validarPermiso(contexto, "F-10-REACTIVAR");

                return legajoService.reactivar(
                                id,
                                contexto.getInstitucionId(),
                                contexto.getCorreo());
        }
        // =========================================================
        // IDENTIDAD Y AUTORIZACION
        // =========================================================

        /**
         * Obtiene el contexto de identidad del usuario autenticado.
         */
        private ContextoIdentidadResponse obtenerContexto(
                        String tenantId,
                        String objectId) {

                return identidadService.consultarContexto(
                                new ContextoIdentidadRequest(tenantId, objectId));
        }

        /**
         * Comprueba que el usuario tenga el permiso requerido.
         */
        private void validarPermiso(
                        ContextoIdentidadResponse contexto,
                        String codigoPermiso) {

                boolean permitido = contexto.getPermisos() != null
                                && contexto.getPermisos().stream()
                                                .anyMatch(p -> codigoPermiso.equals(p.getCodigo())
                                                                && Boolean.TRUE.equals(p.getPermitido()));

                if (!permitido) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "No tienes permiso para realizar esta operación");
                }
        }
}

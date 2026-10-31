// Paquete correspondiente al controlador del módulo Legajo.
package cl.legga.legajos.legajo.controller;

// Importa la entidad y el servicio desde el módulo Legajo.
import cl.legga.legajos.legajo.entity.Legajo;
import cl.legga.legajos.legajo.service.LegajoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/legajos")
public class LegajoController {

    private final LegajoService legajoService;

    public LegajoController(LegajoService legajoService) {
        this.legajoService = legajoService;
    }

    @GetMapping
    public List<Legajo> listar(
            @RequestParam(required = false) String estado) {

        if (estado != null) {
            return legajoService.listarPorEstado(estado);
        }

        return legajoService.listar();
    }

    @GetMapping("/{id}")
    public Legajo buscarPorId(@PathVariable UUID id) {
        return legajoService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Legajo crear(
            @RequestBody Legajo legajo,
            @RequestParam UUID categoriaId) {

        return legajoService.crear(legajo, categoriaId);
    }

    /**
     * Archiva un legajo existente.
     *
     * Método HTTP: PATCH
     * Ruta: /api/legajos/{id}/archivar
     *
     * @param id identificador UUID del legajo.
     * @return legajo con su estado actualizado.
     */
    @PatchMapping("/{id}/archivar")
    public Legajo archivar(@PathVariable UUID id) {

        // Delegar la operación y sus reglas de negocio al servicio.
        return legajoService.archivar(id);
    }

    // Busca legajos por título y/o categoría.
    // Ambos parámetros son opcionales.
    @GetMapping("/buscar")
    public List<Legajo> buscar(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) UUID categoriaId) {

        // Exige al menos un criterio de búsqueda.
        if ((titulo == null || titulo.isBlank()) && categoriaId == null) {
            throw new IllegalArgumentException(
                    "Debe indicar un título o una categoría para buscar");
        }

        // Delega la búsqueda al servicio de legajos.
        return legajoService.buscar(titulo, categoriaId);
    }
}
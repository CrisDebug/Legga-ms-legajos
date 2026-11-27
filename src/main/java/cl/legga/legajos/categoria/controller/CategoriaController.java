
package cl.legga.legajos.categoria.controller;

import cl.legga.legajos.categoria.entity.Categoria;
import cl.legga.legajos.categoria.service.CategoriaService;
import cl.legga.legajos.identidad.dto.ContextoIdentidadRequest;
import cl.legga.legajos.identidad.service.IdentidadService;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;
    private final IdentidadService identidadService;

    public CategoriaController(
            CategoriaService categoriaService,
            IdentidadService identidadService) {
        this.categoriaService = categoriaService;
        this.identidadService = identidadService;
    }

    @GetMapping
    public Page<Categoria> listar(
            @RequestParam(required = false) Boolean activa,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {

        return categoriaService.listar(activa, page, size);
    }

    @GetMapping("/{id}")
    public Categoria buscarPorId(@PathVariable UUID id) {
        return categoriaService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Categoria crear(
            @RequestBody Categoria categoria,
            @RequestHeader("X-LEGGA-TENANT-ID") String tenantId,
            @RequestHeader("X-LEGGA-OBJECT-ID") String objectId) {

        var contexto = identidadService.consultarContexto(
                new ContextoIdentidadRequest(tenantId, objectId));

        return categoriaService.crear(
                categoria,
                contexto.getInstitucionId());
    }
}

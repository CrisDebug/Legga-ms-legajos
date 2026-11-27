package cl.legga.legajos.categoria.documento.controller;

import cl.legga.legajos.categoria.documento.entity.CategoriaDocumentoRequerido;
import cl.legga.legajos.categoria.documento.service.CategoriaDocumentoRequeridoService;
import cl.legga.legajos.identidad.dto.ContextoIdentidadRequest;
import cl.legga.legajos.identidad.service.IdentidadService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categorias/{categoriaId}/requisitos-documentales")
public class CategoriaDocumentoRequeridoController {

    private final CategoriaDocumentoRequeridoService requisitoService;
    private final IdentidadService identidadService;

    public CategoriaDocumentoRequeridoController(
            CategoriaDocumentoRequeridoService requisitoService,
            IdentidadService identidadService) {
        this.requisitoService = requisitoService;
        this.identidadService = identidadService;
    }

    @GetMapping
    public List<CategoriaDocumentoRequerido> listar(
            @PathVariable UUID categoriaId,
            @RequestHeader("X-LEGGA-TENANT-ID") String tenantId,
            @RequestHeader("X-LEGGA-OBJECT-ID") String objectId) {

        var contexto = identidadService.consultarContexto(
                new ContextoIdentidadRequest(tenantId, objectId));

        return requisitoService.listar(
                categoriaId,
                contexto.getInstitucionId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaDocumentoRequerido crear(
            @PathVariable UUID categoriaId,
            @RequestBody CategoriaDocumentoRequerido requisito,
            @RequestHeader("X-LEGGA-TENANT-ID") String tenantId,
            @RequestHeader("X-LEGGA-OBJECT-ID") String objectId) {

        var contexto = identidadService.consultarContexto(
                new ContextoIdentidadRequest(tenantId, objectId));

        return requisitoService.crear(
                categoriaId,
                contexto.getInstitucionId(),
                requisito);
    }
}
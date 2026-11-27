
package cl.legga.legajos.categoria.formulariodinamico.controller;

import cl.legga.legajos.categoria.formulariodinamico.entity.CategoriaCampo;
import cl.legga.legajos.categoria.formulariodinamico.service.CategoriaCampoService;
import cl.legga.legajos.identidad.dto.ContextoIdentidadRequest;
import cl.legga.legajos.identidad.service.IdentidadService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categorias/{categoriaId}/campos")
public class CategoriaCampoController {

    private final CategoriaCampoService campoService;
    private final IdentidadService identidadService;

    public CategoriaCampoController(
            CategoriaCampoService campoService,
            IdentidadService identidadService) {
        this.campoService = campoService;
        this.identidadService = identidadService;
    }

    @GetMapping
    public List<CategoriaCampo> listar(
            @PathVariable UUID categoriaId,
            @RequestHeader("X-LEGGA-TENANT-ID") String tenantId,
            @RequestHeader("X-LEGGA-OBJECT-ID") String objectId) {

        var contexto = identidadService.consultarContexto(
                new ContextoIdentidadRequest(tenantId, objectId));

        return campoService.listar(
                categoriaId,
                contexto.getInstitucionId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaCampo crear(
            @PathVariable UUID categoriaId,
            @RequestBody CategoriaCampo campo,
            @RequestHeader("X-LEGGA-TENANT-ID") String tenantId,
            @RequestHeader("X-LEGGA-OBJECT-ID") String objectId) {

        var contexto = identidadService.consultarContexto(
                new ContextoIdentidadRequest(tenantId, objectId));

        return campoService.crear(
                categoriaId,
                contexto.getInstitucionId(),
                campo);
    }

    @PutMapping("/{campoId}")
    public CategoriaCampo actualizar(
            @PathVariable UUID categoriaId,
            @PathVariable UUID campoId,
            @RequestBody CategoriaCampo campo,
            @RequestHeader("X-LEGGA-TENANT-ID") String tenantId,
            @RequestHeader("X-LEGGA-OBJECT-ID") String objectId) {

        var contexto = identidadService.consultarContexto(
                new ContextoIdentidadRequest(tenantId, objectId));

        return campoService.actualizar(
                categoriaId,
                contexto.getInstitucionId(),
                campoId,
                campo);
    }

    @DeleteMapping("/{campoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(
            @PathVariable UUID categoriaId,
            @PathVariable UUID campoId,
            @RequestHeader("X-LEGGA-TENANT-ID") String tenantId,
            @RequestHeader("X-LEGGA-OBJECT-ID") String objectId) {

        var contexto = identidadService.consultarContexto(
                new ContextoIdentidadRequest(tenantId, objectId));

        campoService.eliminar(
                categoriaId,
                contexto.getInstitucionId(),
                campoId);
    }
}

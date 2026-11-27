package cl.legga.legajos.categoria.documento.service;

import cl.legga.legajos.categoria.documento.entity.CategoriaDocumentoRequerido;
import cl.legga.legajos.categoria.documento.repository.CategoriaDocumentoRequeridoRepository;
import cl.legga.legajos.categoria.entity.Categoria;
import cl.legga.legajos.categoria.repository.CategoriaRepository;
import cl.legga.legajos.exception.RecursoNoEncontradoException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CategoriaDocumentoRequeridoService {

    private final CategoriaDocumentoRequeridoRepository requisitoRepository;
    private final CategoriaRepository categoriaRepository;

    public CategoriaDocumentoRequeridoService(
            CategoriaDocumentoRequeridoRepository requisitoRepository,
            CategoriaRepository categoriaRepository) {
        this.requisitoRepository = requisitoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaDocumentoRequerido> listar(
            UUID categoriaId,
            UUID institucionId) {

        validarCategoriaInstitucion(categoriaId, institucionId);

        return requisitoRepository
                .findByCategoriaIdAndActivoTrueOrderByOrdenAsc(categoriaId);
    }

    @Transactional
    public CategoriaDocumentoRequerido crear(
            UUID categoriaId,
            UUID institucionId,
            CategoriaDocumentoRequerido requisito) {

        validarCategoriaInstitucion(categoriaId, institucionId);

        if (requisito == null) {
            throw new IllegalArgumentException(
                    "Los datos del requisito son obligatorios");
        }

        if (requisito.getCodigo() == null
                || requisito.getCodigo().isBlank()) {
            throw new IllegalArgumentException(
                    "El código del requisito es obligatorio");
        }

        if (requisito.getNombre() == null
                || requisito.getNombre().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del requisito es obligatorio");
        }

        String codigo = requisito.getCodigo().trim();
        String nombre = requisito.getNombre().trim();

        if (requisitoRepository.existsByCategoriaIdAndCodigo(
                categoriaId, codigo)) {
            throw new IllegalArgumentException(
                    "Ya existe un requisito con ese código en la categoría");
        }

        requisito.setId(null);
        requisito.setCategoriaId(categoriaId);
        requisito.setCodigo(codigo);
        requisito.setNombre(nombre);
        requisito.setActivo(true);

        if (requisito.getObligatorio() == null) {
            requisito.setObligatorio(true);
        }

        if (requisito.getOrden() == null) {
            requisito.setOrden(0);
        }

        return requisitoRepository.save(requisito);
    }

    private void validarCategoriaInstitucion(
            UUID categoriaId,
            UUID institucionId) {

        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Categoría no encontrada"));

        if (!categoria.getInstitucionId().equals(institucionId)) {
            throw new RecursoNoEncontradoException(
                    "Categoría no encontrada");
        }
    }
}
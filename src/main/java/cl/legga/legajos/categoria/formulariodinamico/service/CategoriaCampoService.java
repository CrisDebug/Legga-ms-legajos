
package cl.legga.legajos.categoria.formulariodinamico.service;

import cl.legga.legajos.categoria.formulariodinamico.entity.CategoriaCampo;
import cl.legga.legajos.categoria.formulariodinamico.repository.CategoriaCampoRepository;
import cl.legga.legajos.categoria.entity.Categoria;
import cl.legga.legajos.categoria.repository.CategoriaRepository;
import cl.legga.legajos.exception.RecursoNoEncontradoException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CategoriaCampoService {

    private final CategoriaCampoRepository campoRepository;
    private final CategoriaRepository categoriaRepository;

    public CategoriaCampoService(
            CategoriaCampoRepository campoRepository,
            CategoriaRepository categoriaRepository) {
        this.campoRepository = campoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaCampo> listar(
            UUID categoriaId,
            UUID institucionId) {

        validarCategoriaInstitucion(categoriaId, institucionId);

        return campoRepository
                .findByCategoriaIdAndActivoTrueOrderByOrdenAsc(categoriaId);
    }

    @Transactional
    public CategoriaCampo crear(
            UUID categoriaId,
            UUID institucionId,
            CategoriaCampo campo) {

        validarCategoriaInstitucion(categoriaId, institucionId);

        if (campo == null) {
            throw new IllegalArgumentException(
                    "Los datos del campo son obligatorios");
        }

        if (campo.getCodigo() == null || campo.getCodigo().isBlank()) {
            throw new IllegalArgumentException(
                    "El código del campo es obligatorio");
        }

        if (campo.getEtiqueta() == null || campo.getEtiqueta().isBlank()) {
            throw new IllegalArgumentException(
                    "La etiqueta del campo es obligatoria");
        }

        if (campo.getTipoDato() == null || campo.getTipoDato().isBlank()) {
            throw new IllegalArgumentException(
                    "El tipo de dato es obligatorio");
        }

        String codigo = campo.getCodigo().trim();

        if (campoRepository.existsByCategoriaIdAndCodigo(
                categoriaId, codigo)) {
            throw new IllegalArgumentException(
                    "Ya existe un campo con ese código en la categoría");
        }

        campo.setId(null);
        campo.setCategoriaId(categoriaId);
        campo.setCodigo(codigo);
        campo.setEtiqueta(campo.getEtiqueta().trim());
        campo.setActivo(true);

        if (campo.getObligatorio() == null) {
            campo.setObligatorio(false);
        }

        if (campo.getOrden() == null) {
            campo.setOrden(0);
        }

        return campoRepository.save(campo);
    }

    @Transactional
    public CategoriaCampo actualizar(
            UUID categoriaId,
            UUID institucionId,
            UUID campoId,
            CategoriaCampo datos) {

        validarCategoriaInstitucion(categoriaId, institucionId);

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos del campo son obligatorios");
        }

        CategoriaCampo campo = campoRepository.findById(campoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Campo de categoría no encontrado"));

        if (!campo.getCategoriaId().equals(categoriaId)) {
            throw new RecursoNoEncontradoException(
                    "Campo de categoría no encontrado");
        }

        if (datos.getCodigo() == null || datos.getCodigo().isBlank()
                || datos.getEtiqueta() == null || datos.getEtiqueta().isBlank()
                || datos.getTipoDato() == null || datos.getTipoDato().isBlank()) {
            throw new IllegalArgumentException(
                    "Código, etiqueta y tipo de dato son obligatorios");
        }

        String codigo = datos.getCodigo().trim();

        if (!campo.getCodigo().equals(codigo)
                && campoRepository.existsByCategoriaIdAndCodigo(
                        categoriaId, codigo)) {
            throw new IllegalArgumentException(
                    "Ya existe un campo con ese código en la categoría");
        }

        campo.setCodigo(codigo);
        campo.setEtiqueta(datos.getEtiqueta().trim());
        campo.setTipoDato(datos.getTipoDato());
        campo.setObligatorio(datos.getObligatorio() != null
                ? datos.getObligatorio()
                : false);
        campo.setOrden(datos.getOrden() != null
                ? datos.getOrden()
                : 0);
        campo.setOpciones(datos.getOpciones());

        return campoRepository.save(campo);
    }

    @Transactional
    public void eliminar(
            UUID categoriaId,
            UUID institucionId,
            UUID campoId) {

        validarCategoriaInstitucion(categoriaId, institucionId);

        CategoriaCampo campo = campoRepository.findById(campoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Campo de categoría no encontrado"));

        if (!campo.getCategoriaId().equals(categoriaId)) {
            throw new RecursoNoEncontradoException(
                    "Campo de categoría no encontrado");
        }

        campo.setActivo(false);

        campoRepository.save(campo);
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

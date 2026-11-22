
package cl.legga.legajos.categoria.service;

import cl.legga.legajos.categoria.entity.Categoria;
import cl.legga.legajos.categoria.repository.CategoriaRepository;
import cl.legga.legajos.exception.RecursoNoEncontradoException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    /**
     * Consulta categorías con filtro opcional y paginación.
     */
    public Page<Categoria> listar(Boolean activa, int page, int size) {

        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException(
                    "Los parámetros de paginación no son válidos");
        }

        Pageable pageable = PageRequest.of(page, size);

        if (activa == null) {
            return categoriaRepository.findAll(pageable);
        }

        return categoriaRepository.findByActiva(activa, pageable);
    }

    public Categoria buscarPorId(UUID id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada"));
    }

    public Categoria crear(Categoria categoria) {

        if (categoriaRepository.existsByNombreIgnoreCase(
                categoria.getNombre())) {
            throw new IllegalArgumentException(
                    "Ya existe una categoría con ese nombre");
        }

        categoria.setId(null);

        return categoriaRepository.save(categoria);
    }
}

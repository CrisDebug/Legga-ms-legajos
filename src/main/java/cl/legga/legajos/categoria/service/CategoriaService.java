package cl.legga.legajos.categoria.service;

import cl.legga.legajos.categoria.entity.Categoria;
import cl.legga.legajos.categoria.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import cl.legga.legajos.exception.RecursoNoEncontradoException;
import java.util.List;
import java.util.UUID;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    public Categoria buscarPorId(UUID id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada"));
    }

    public Categoria crear(Categoria categoria) {
        if (categoriaRepository.existsByNombreIgnoreCase(categoria.getNombre())) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre");
        }

        categoria.setId(null);
        return categoriaRepository.save(categoria);
    }
}
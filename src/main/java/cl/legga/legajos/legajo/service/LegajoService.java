// Paquete correspondiente al servicio del módulo Legajo.
package cl.legga.legajos.legajo.service;

// Importa la entidad Legajo desde su nuevo módulo.
import cl.legga.legajos.legajo.entity.Legajo;

// Importa el repositorio de Legajo desde su nuevo módulo.
import cl.legga.legajos.legajo.repository.LegajoRepository;

// Mantiene Categoria en su ubicación actual.
import cl.legga.legajos.categoria.entity.Categoria;

//excepciones goblales
import cl.legga.legajos.exception.RecursoNoEncontradoException;
// Mantiene el repositorio de Categoria en su ubicación actual.
import cl.legga.legajos.categoria.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class LegajoService {

    private final LegajoRepository legajoRepository;
    private final CategoriaRepository categoriaRepository;

    public LegajoService(
            LegajoRepository legajoRepository,
            CategoriaRepository categoriaRepository) {
        this.legajoRepository = legajoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<Legajo> listar() {
        return legajoRepository.findAll();
    }

    public Legajo buscarPorId(UUID id) {
        return legajoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Legajo no encontrado"));
    }

    public List<Legajo> listarPorEstado(String estado) {
        validarEstado(estado);
        return legajoRepository.findByEstado(estado);
    }

    @Transactional
    public Legajo crear(Legajo legajo, UUID categoriaId) {

        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        if (!Boolean.TRUE.equals(categoria.getActiva())) {
            throw new IllegalArgumentException(
                    "No se puede crear un legajo con una categoría inactiva");
        }

        legajo.setId(null);
        legajo.setCategoria(categoria);
        legajo.setEstado("ACTIVO");

        return legajoRepository.save(legajo);
    }

    private void validarEstado(String estado) {
        if (!"ACTIVO".equals(estado) && !"ARCHIVADO".equals(estado)) {
            throw new IllegalArgumentException("Estado de legajo inválido");
        }
    }

    /**
     * Archiva un legajo existente.
     *
     * Busca el legajo por su identificador UUID.
     * Verifica que exista y que todavía no esté archivado.
     * Finalmente, actualiza su estado a ARCHIVADO.
     *
     * @param id identificador único del legajo.
     * @return legajo actualizado.
     */
    @Transactional
    public Legajo archivar(UUID id) {

        // Buscar el legajo en la base de datos.
        Legajo legajo = legajoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Legajo no encontrado"));

        // Evitar archivar nuevamente un legajo archivado.
        if ("ARCHIVADO".equals(legajo.getEstado())) {
            throw new IllegalArgumentException(
                    "El legajo ya se encuentra archivado");
        }

        // Actualizar el estado del legajo.
        legajo.setEstado("ARCHIVADO");

        // Guardar los cambios en PostgreSQL.
        return legajoRepository.save(legajo);
    }

    // Busca legajos por título y/o categoría.
    // Si un criterio es nulo, no se aplica ese filtro.
    @Transactional(readOnly = true)
    public List<Legajo> buscar(String titulo, UUID categoriaId) {

        // Evita enviar textos vacíos como criterios de búsqueda.
        if (titulo != null && titulo.isBlank()) {
            titulo = null;
        }

        // Ejecuta la búsqueda utilizando los filtros recibidos.
        return legajoRepository.buscarPorTituloYCategoria(titulo, categoriaId);
    }
}
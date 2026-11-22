package cl.legga.legajos.legajo.repository;

import cl.legga.legajos.legajo.entity.Legajo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LegajoRepository extends JpaRepository<Legajo, UUID>, JpaSpecificationExecutor<Legajo> {

    List<Legajo> findByEstado(String estado);

    List<Legajo> findByCategoriaId(UUID categoriaId);

    // Consultas restringidas a una institución.
    List<Legajo> findAllByInstitucionIdOrderByCreatedAtDesc(UUID institucionId);

    List<Legajo> findByInstitucionIdAndEstadoOrderByCreatedAtDesc(
            UUID institucionId, String estado);

    Optional<Legajo> findByIdAndInstitucionId(UUID id, UUID institucionId);

    @Query("""
            SELECT l
            FROM Legajo l
            WHERE l.institucionId = :institucionId
              AND (:titulo IS NULL OR
                   LOWER(l.titulo) LIKE LOWER(CONCAT('%', :titulo, '%')))
              AND (:categoriaId IS NULL OR l.categoria.id = :categoriaId)
            ORDER BY l.createdAt DESC
            """)
    List<Legajo> buscarPorTituloYCategoriaEInstitucion(
            @Param("titulo") String titulo,
            @Param("categoriaId") UUID categoriaId,
            @Param("institucionId") UUID institucionId);

    // Se conserva la búsqueda anterior por compatibilidad.
    @Query("""
            SELECT l
            FROM Legajo l
            WHERE (:titulo IS NULL OR
                   LOWER(l.titulo) LIKE LOWER(CONCAT('%', :titulo, '%')))
              AND (:categoriaId IS NULL OR l.categoria.id = :categoriaId)
            ORDER BY l.createdAt DESC
            """)
    List<Legajo> buscarPorTituloYCategoria(
            @Param("titulo") String titulo,
            @Param("categoriaId") UUID categoriaId);

    // =========================================================
    // CONSULTA PAGINADA POR INSTITUCION
    // =========================================================

    @Query(
        value = """
            SELECT l
            FROM Legajo l
            WHERE l.institucionId = :institucionId
              AND (:titulo IS NULL OR
                   LOWER(l.titulo) LIKE LOWER(CONCAT('%', :titulo, '%')))
              AND (:categoriaId IS NULL OR l.categoria.id = :categoriaId)
              AND (:estado IS NULL OR l.estado = :estado)
            ORDER BY l.createdAt DESC
            """,
        countQuery = """
            SELECT COUNT(l)
            FROM Legajo l
            WHERE l.institucionId = :institucionId
              AND (:titulo IS NULL OR
                   LOWER(l.titulo) LIKE LOWER(CONCAT('%', :titulo, '%')))
              AND (:categoriaId IS NULL OR l.categoria.id = :categoriaId)
              AND (:estado IS NULL OR l.estado = :estado)
            """
    )
    Page<Legajo> buscarPaginadoPorInstitucion(
            @Param("institucionId") UUID institucionId,
            @Param("titulo") String titulo,
            @Param("categoriaId") UUID categoriaId,
            @Param("estado") String estado,
            Pageable pageable);
}
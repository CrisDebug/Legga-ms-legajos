
// Paquete correspondiente al repositorio del módulo Legajo.
package cl.legga.legajos.legajo.repository;

import cl.legga.legajos.legajo.entity.Legajo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

// Repositorio encargado del acceso a datos de los legajos.
public interface LegajoRepository extends JpaRepository<Legajo, UUID> {

        // Obtiene los legajos según su estado administrativo.
        List<Legajo> findByEstado(String estado);

        // Obtiene los legajos asociados a una categoría.
        List<Legajo> findByCategoriaId(UUID categoriaId);

        // Busca legajos por título y/o categoría.
        // Ambos parámetros son opcionales y pueden combinarse.
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
}

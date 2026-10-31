package cl.legga.legajos.categoria.repository;

import cl.legga.legajos.categoria.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {

    boolean existsByNombreIgnoreCase(String nombre);
}
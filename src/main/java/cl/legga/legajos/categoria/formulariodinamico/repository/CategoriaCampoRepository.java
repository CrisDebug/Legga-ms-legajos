
package cl.legga.legajos.categoria.formulariodinamico.repository;

import cl.legga.legajos.categoria.formulariodinamico.entity.CategoriaCampo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoriaCampoRepository
        extends JpaRepository<CategoriaCampo, UUID> {

    List<CategoriaCampo> findByCategoriaIdAndActivoTrueOrderByOrdenAsc(UUID categoriaId);

    boolean existsByCategoriaIdAndCodigo(UUID categoriaId, String codigo);
}

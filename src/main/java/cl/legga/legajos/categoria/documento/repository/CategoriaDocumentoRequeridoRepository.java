package cl.legga.legajos.categoria.documento.repository;

import cl.legga.legajos.categoria.documento.entity.CategoriaDocumentoRequerido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoriaDocumentoRequeridoRepository
        extends JpaRepository<CategoriaDocumentoRequerido, UUID> {

    List<CategoriaDocumentoRequerido>
            findByCategoriaIdAndActivoTrueOrderByOrdenAsc(UUID categoriaId);

    boolean existsByCategoriaIdAndCodigo(UUID categoriaId, String codigo);
}
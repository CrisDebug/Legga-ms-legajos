package cl.legga.legajos.legajo.dto;

import cl.legga.legajos.legajo.entity.Legajo;
import java.time.OffsetDateTime;
import java.util.UUID;

public class LegajoResponseDTO {

    private UUID id;
    private String titulo;
    private String descripcion;
    private UUID categoriaId;
    private String categoriaNombre;
    private String estado;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public LegajoResponseDTO(Legajo legajo) {
        this.id = legajo.getId();
        this.titulo = legajo.getTitulo();
        this.descripcion = legajo.getDescripcion();
        this.categoriaId = legajo.getCategoria().getId();
        this.categoriaNombre = legajo.getCategoria().getNombre();
        this.estado = legajo.getEstado();
        this.createdAt = legajo.getCreatedAt();
        this.updatedAt = legajo.getUpdatedAt();
    }

    public UUID getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public UUID getCategoriaId() { return categoriaId; }
    public String getCategoriaNombre() { return categoriaNombre; }
    public String getEstado() { return estado; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
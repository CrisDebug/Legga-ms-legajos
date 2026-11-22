
package cl.legga.legajos.identidad.dto;

import java.util.List;
import java.util.UUID;

/**
 * DTO de respuesta del contexto de identidad de LEGGA.
 *
 * Contiene los datos del usuario, su institución,
 * su perfil de aplicación y los permisos funcionales
 * asociados a dicho perfil.
 *
 * La autorización efectiva también dependerá del
 * ámbito y del recurso solicitado.
 */
public class ContextoIdentidadResponse {

    // Identificador interno del usuario en PostgreSQL.
    private UUID usuarioId;

    // Correo registrado en LEGGA.
    private String correo;

    // Nombre visible del usuario.
    private String nombre;

    // Estado de activación del usuario.
    private Boolean activo;

    // Identificación de la institución asociada.
    private UUID institucionId;
    private String institucionCodigo;
    private String institucionNombre;

    // Identificación del perfil de aplicación.
    private UUID perfilId;
    private String perfilCodigo;
    private String perfilNombre;

    // Operaciones específicas autorizadas o denegadas.
    private List<PermisoDTO> permisos;

    public ContextoIdentidadResponse() {
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public UUID getInstitucionId() {
        return institucionId;
    }

    public void setInstitucionId(UUID institucionId) {
        this.institucionId = institucionId;
    }

    public String getInstitucionCodigo() {
        return institucionCodigo;
    }

    public void setInstitucionCodigo(String institucionCodigo) {
        this.institucionCodigo = institucionCodigo;
    }

    public String getInstitucionNombre() {
        return institucionNombre;
    }

    public void setInstitucionNombre(String institucionNombre) {
        this.institucionNombre = institucionNombre;
    }

    public UUID getPerfilId() {
        return perfilId;
    }

    public void setPerfilId(UUID perfilId) {
        this.perfilId = perfilId;
    }

    public String getPerfilCodigo() {
        return perfilCodigo;
    }

    public void setPerfilCodigo(String perfilCodigo) {
        this.perfilCodigo = perfilCodigo;
    }

    public String getPerfilNombre() {
        return perfilNombre;
    }

    public void setPerfilNombre(String perfilNombre) {
        this.perfilNombre = perfilNombre;
    }

    public List<PermisoDTO> getPermisos() {
        return permisos;
    }

    public void setPermisos(List<PermisoDTO> permisos) {
        this.permisos = permisos;
    }
}


package cl.legga.legajos.identidad.dto;

/**
 * DTO que representa el permiso de un usuario
 * sobre una funcionalidad específica de LEGGA.
 *
 * El permiso indica si el perfil asociado al usuario
 * tiene habilitada esa funcionalidad.
 */
public class PermisoDTO {

    /**
     * Código único de la funcionalidad.
     *
     * Ejemplos:
     * F-01, F-02, F-03, F-04.
     */
    private String codigo;

    /**
     * Indica si la funcionalidad está permitida
     * para el perfil del usuario.
     */
    private Boolean permitido;

    /**
     * Constructor vacío requerido para la serialización.
     */
    public PermisoDTO() {
    }

    /**
     * Constructor con todos los atributos.
     */
    public PermisoDTO(String codigo, Boolean permitido) {
        this.codigo = codigo;
        this.permitido = permitido;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Boolean getPermitido() {
        return permitido;
    }

    public void setPermitido(Boolean permitido) {
        this.permitido = permitido;
    }
}

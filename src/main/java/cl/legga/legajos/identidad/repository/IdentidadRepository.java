
package cl.legga.legajos.identidad.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio encargado de consultar la identidad,
 * institución, perfil y permisos de los usuarios LEGGA.
 *
 * Todas las consultas son de solo lectura.
 */
@Repository
public class IdentidadRepository {

    private final JdbcTemplate jdbcTemplate;

    public IdentidadRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Recupera las asociaciones activas de un usuario
     * utilizando su identidad estable de Microsoft Entra.
     */
    public List<ContextoBase> buscarContextosActivos(
            String tenantId,
            String objectId) {

        String sql = """
                SELECT
                    u.id,
                    u.correo,
                    u.nombre,
                    u.activo,
                    i.id AS institucion_id,
                    i.codigo AS institucion_codigo,
                    i.nombre AS institucion_nombre,
                    p.id AS perfil_id,
                    p.codigo AS perfil_codigo,
                    p.nombre AS perfil_nombre
                FROM seguridad.usuario u
                INNER JOIN seguridad.usuario_institucion ui
                    ON ui.usuario_id = u.id
                INNER JOIN seguridad.institucion i
                    ON i.id = ui.institucion_id
                INNER JOIN seguridad.perfil p
                    ON p.id = ui.perfil_id
                WHERE u.entra_tenant_id = ?
                  AND u.entra_object_id = ?
                  AND u.activo = TRUE
                  AND ui.activo = TRUE
                  AND i.activo = TRUE
                  AND p.activo = TRUE
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new ContextoBase(
                        rs.getObject("id", UUID.class),
                        rs.getString("correo"),
                        rs.getString("nombre"),
                        rs.getBoolean("activo"),
                        rs.getObject("institucion_id", UUID.class),
                        rs.getString("institucion_codigo"),
                        rs.getString("institucion_nombre"),
                        rs.getObject("perfil_id", UUID.class),
                        rs.getString("perfil_codigo"),
                        rs.getString("perfil_nombre")),
                tenantId,
                objectId);
    }

    /**
     * Recupera las 19 operaciones registradas en el catálogo
     * y sus permisos asociados al perfil.
     *
     * Las operaciones sin asignación se consideran denegadas.
     */
    public List<PermisoBase> buscarPermisosPorPerfil(UUID perfilId) {

        String sql = """
                SELECT
                    f.codigo,
                    COALESCE(pp.permitido, FALSE) AS permitido
                FROM seguridad.funcionalidad f
                LEFT JOIN seguridad.perfil_permiso pp
                    ON pp.funcionalidad_id = f.id
                   AND pp.perfil_id = ?
                WHERE f.activo = TRUE
                ORDER BY f.codigo
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new PermisoBase(
                        rs.getString("codigo"),
                        rs.getBoolean("permitido")),
                perfilId);
    }

    /**
     * Representa los datos recuperados del usuario,
     * su institución y su perfil.
     */
    public record ContextoBase(
            UUID usuarioId,
            String correo,
            String nombre,
            Boolean activo,
            UUID institucionId,
            String institucionCodigo,
            String institucionNombre,
            UUID perfilId,
            String perfilCodigo,
            String perfilNombre) {
    }

    /**
     * Representa una operación y su autorización.
     */
    public record PermisoBase(
            String codigo,
            Boolean permitido) {
    }
}

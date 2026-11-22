
package cl.legga.legajos.identidad.service;

import cl.legga.legajos.identidad.dto.ContextoIdentidadRequest;
import cl.legga.legajos.identidad.dto.ContextoIdentidadResponse;
import cl.legga.legajos.identidad.dto.PermisoDTO;
import cl.legga.legajos.identidad.repository.IdentidadRepository;
import cl.legga.legajos.exception.RecursoNoEncontradoException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio encargado de resolver el contexto de identidad
 * y los permisos funcionales de los usuarios LEGGA.
 *
 * No autentica usuarios ni valida tokens de Microsoft Entra.
 * Esa responsabilidad corresponde al BFF.
 */
@Service
public class IdentidadService {

    private final IdentidadRepository identidadRepository;

    public IdentidadService(IdentidadRepository identidadRepository) {
        this.identidadRepository = identidadRepository;
    }

    /**
     * Busca la identidad mediante tenantId y objectId,
     * recupera su institución y perfil y obtiene los permisos.
     */
    @Transactional(readOnly = true)
    public ContextoIdentidadResponse consultarContexto(
            ContextoIdentidadRequest request) {

        // Recuperar las asociaciones activas del usuario.
        var contextos = identidadRepository.buscarContextosActivos(
                request.getTenantId(),
                request.getObjectId());

        // Si no existe una asociación activa, no hay contexto disponible.
        if (contextos.isEmpty()) {
            throw new RecursoNoEncontradoException(
                    "No existe un usuario activo asociado a la identidad recibida.");
        }

        // No seleccionar arbitrariamente una institución o perfil.
        if (contextos.size() > 1) {
            throw new IllegalStateException(
                    "El usuario tiene múltiples asociaciones activas. "
                            + "Se requiere resolver la institución antes de continuar.");
        }

        var contexto = contextos.get(0);

        // Consultar las operaciones y autorizaciones del perfil.
        List<PermisoDTO> permisos = identidadRepository.buscarPermisosPorPerfil(
                contexto.perfilId())
                .stream()
                .map(permiso -> new PermisoDTO(
                        permiso.codigo(),
                        permiso.permitido()))
                .toList();

        // Construir la respuesta que consumirá el BFF.
        ContextoIdentidadResponse response = new ContextoIdentidadResponse();

        response.setUsuarioId(contexto.usuarioId());
        response.setCorreo(contexto.correo());
        response.setNombre(contexto.nombre());
        response.setActivo(contexto.activo());

        response.setInstitucionId(contexto.institucionId());
        response.setInstitucionCodigo(contexto.institucionCodigo());
        response.setInstitucionNombre(contexto.institucionNombre());

        response.setPerfilId(contexto.perfilId());
        response.setPerfilCodigo(contexto.perfilCodigo());
        response.setPerfilNombre(contexto.perfilNombre());

        response.setPermisos(permisos);

        return response;
    }
}


package cl.legga.legajos.legajo.service;

import cl.legga.legajos.legajo.entity.Legajo;
import cl.legga.legajos.legajo.repository.LegajoRepository;
import cl.legga.legajos.legajo.dto.LegajoResponseDTO;
import cl.legga.legajos.categoria.entity.Categoria;
import cl.legga.legajos.categoria.repository.CategoriaRepository;
import cl.legga.legajos.exception.RecursoNoEncontradoException;
import cl.legga.legajos.outbox.OutboxEventService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class LegajoService {

    private final LegajoRepository legajoRepository;
    private final CategoriaRepository categoriaRepository;
    private final OutboxEventService outboxEventService;

    private static final Set<String> CAMPOS_EDITABLES = Set.of("titulo", "descripcion", "categoriaId");

    public LegajoService(
            LegajoRepository legajoRepository,
            CategoriaRepository categoriaRepository,
            OutboxEventService outboxEventService) {
        this.legajoRepository = legajoRepository;
        this.categoriaRepository = categoriaRepository;
        this.outboxEventService = outboxEventService;
    }

    // =========================================================
    // CONSULTAS RESTRINGIDAS POR INSTITUCION
    // =========================================================

    /**
     * Lista los legajos pertenecientes a una instituciÃ³n.
     */
    @Transactional(readOnly = true)
    public List<Legajo> listarPorInstitucion(UUID institucionId) {
        validarInstitucion(institucionId);

        return legajoRepository.findAllByInstitucionIdOrderByCreatedAtDesc(
                institucionId);
    }

    /**
     * Lista los legajos de una instituciÃ³n filtrados por estado.
     */
    @Transactional(readOnly = true)
    public List<Legajo> listarPorInstitucionYEstado(
            UUID institucionId, String estado) {

        validarInstitucion(institucionId);
        validarEstado(estado);

        return legajoRepository.findByInstitucionIdAndEstadoOrderByCreatedAtDesc(
                institucionId, estado);
    }

    /**
     * Busca un legajo por su identificador y la instituciÃ³n propietaria.
     *
     * Este mÃ©todo conserva la consulta original y se mantiene
     * para los procesos internos que necesitan trabajar con la entidad.
     */
    @Transactional(readOnly = true)
    public Legajo buscarPorIdEInstitucion(UUID id, UUID institucionId) {

        validarInstitucion(institucionId);

        return legajoRepository.findByIdAndInstitucionId(id, institucionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Legajo no encontrado"));
    }

    /**
     * Busca un legajo y devuelve su DTO de respuesta.
     *
     * La conversiÃ³n a DTO ocurre dentro de la transacciÃ³n activa.
     * Esto permite acceder a la categorÃ­a configurada como LAZY
     * y obtener categoriaId y categoriaNombre correctamente.
     *
     * Este mÃ©todo debe utilizarse en el endpoint de detalle.
     */
    @Transactional(readOnly = true)
    public LegajoResponseDTO buscarPorIdEInstitucionDTO(
            UUID id,
            UUID institucionId) {

        validarInstitucion(institucionId);

        Legajo legajo = legajoRepository.findByIdAndInstitucionId(id, institucionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Legajo no encontrado"));

        return new LegajoResponseDTO(legajo);
    }

    /**
     * Busca legajos por tÃ­tulo y categorÃ­a dentro de una instituciÃ³n.
     */
    @Transactional(readOnly = true)
    public List<Legajo> buscarPorInstitucion(
            String titulo, UUID categoriaId, UUID institucionId) {

        validarInstitucion(institucionId);

        if (titulo != null && titulo.isBlank()) {
            titulo = null;
        }

        return legajoRepository.buscarPorTituloYCategoriaEInstitucion(
                titulo, categoriaId, institucionId);
    }

    // =========================================================
    // CREACION
    // =========================================================

    /**
     * Crea un nuevo legajo asociado a una instituciÃ³n y categorÃ­a.
     */
    @Transactional
    public Legajo crear(
            Legajo legajo, UUID categoriaId, UUID institucionId) {

        validarInstitucion(institucionId);
        validarTitulo(legajo.getTitulo());

        Categoria categoria = obtenerCategoriaActiva(categoriaId);

        legajo.setId(null);
        legajo.setCategoria(categoria);
        legajo.setInstitucionId(institucionId);
        legajo.setEstado("ACTIVO");

        Legajo creado = legajoRepository.save(legajo);

        registrarAuditoria(
                "LEGAJO_CREATED",
                creado,
                "CREACION",
                creado.getCreadoPor());

        return creado;
    }

    // =========================================================
    // EDICION COMPLETA - PUT
    // =========================================================

    /**
     * Reemplaza los datos editables de un legajo.
     */
    @Transactional
    public Legajo reemplazar(
            UUID id,
            UUID institucionId,
            String titulo,
            String descripcion,
            UUID categoriaId,
            String actor) {

        Legajo legajo = buscarPorIdEInstitucion(id, institucionId);

        if ("ARCHIVADO".equals(legajo.getEstado())) {
            throw new IllegalArgumentException(
                    "No se puede editar un legajo archivado");
        }

        validarTitulo(titulo);
        validarDescripcion(descripcion);

        Categoria categoria = obtenerCategoriaActiva(categoriaId);

        legajo.setTitulo(titulo.trim());
        legajo.setDescripcion(descripcion);
        legajo.setCategoria(categoria);

        Legajo actualizado = legajoRepository.save(legajo);

        registrarAuditoria(
                "LEGAJO_UPDATED",
                actualizado,
                "PUT",
                actor);

        return actualizado;
    }

    // =========================================================
    // EDICION PARCIAL - PATCH
    // =========================================================

    /**
     * Actualiza parcialmente un legajo.
     *
     * Solo permite modificar los campos definidos en CAMPOS_EDITABLES.
     */
    @Transactional
    public Legajo actualizarParcialmente(
            UUID id,
            UUID institucionId,
            Map<String, Object> cambios,
            String actor) {

        if (cambios == null || cambios.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe enviar al menos un campo para actualizar");
        }

        for (String campo : cambios.keySet()) {
            if (!CAMPOS_EDITABLES.contains(campo)) {
                throw new IllegalArgumentException(
                        "El campo no es editable: " + campo);
            }
        }

        Legajo legajo = buscarPorIdEInstitucion(id, institucionId);

        if ("ARCHIVADO".equals(legajo.getEstado())) {
            throw new IllegalArgumentException(
                    "No se puede editar un legajo archivado");
        }

        if (cambios.containsKey("titulo")) {
            Object valor = cambios.get("titulo");

            if (!(valor instanceof String titulo)) {
                throw new IllegalArgumentException(
                        "El tÃ­tulo debe ser un texto");
            }

            validarTitulo(titulo);
            legajo.setTitulo(titulo.trim());
        }

        if (cambios.containsKey("descripcion")) {
            Object valor = cambios.get("descripcion");

            if (valor != null && !(valor instanceof String)) {
                throw new IllegalArgumentException(
                        "La descripciÃ³n debe ser un texto");
            }

            String descripcion = (String) valor;
            validarDescripcion(descripcion);
            legajo.setDescripcion(descripcion);
        }

        if (cambios.containsKey("categoriaId")) {
            Object valor = cambios.get("categoriaId");

            if (!(valor instanceof String categoriaTexto)) {
                throw new IllegalArgumentException(
                        "categoriaId debe ser un UUID vÃ¡lido");
            }

            UUID categoriaId;

            try {
                categoriaId = UUID.fromString(categoriaTexto);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(
                        "categoriaId debe ser un UUID vÃ¡lido");
            }

            legajo.setCategoria(obtenerCategoriaActiva(categoriaId));
        }

        Legajo actualizado = legajoRepository.save(legajo);

        registrarAuditoria(
                "LEGAJO_UPDATED",
                actualizado,
                "PATCH",
                actor);

        return actualizado;
    }

    // =========================================================
    // ARCHIVADO
    // =========================================================

    /**
     * Archiva un legajo por identificador.
     */
    @Transactional
    public Legajo archivar(UUID id) {

        Legajo legajo = legajoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Legajo no encontrado"));

        return archivarLegajo(legajo);
    }

    /**
     * Archiva un legajo validando su instituciÃ³n.
     */
    @Transactional
    public Legajo archivar(UUID id, UUID institucionId, String actor) {

        Legajo legajo = buscarPorIdEInstitucion(id, institucionId);

        return archivarLegajo(legajo);
    }

    /**
     * Ejecuta la operaciÃ³n de archivado.
     */
    private Legajo archivarLegajo(Legajo legajo) {

        if ("ARCHIVADO".equals(legajo.getEstado())) {
            throw new IllegalArgumentException(
                    "El legajo ya se encuentra archivado");
        }

        legajo.setEstado("ARCHIVADO");

        return legajoRepository.save(legajo);
    }

    // =========================================================
    // =========================================================
    // REACTIVACION
    // =========================================================

    /**
     * Reactiva un legajo validando su instituciÃ³n.
     */
    @Transactional
    public Legajo reactivar(UUID id, UUID institucionId, String actor) {

        Legajo legajo = buscarPorIdEInstitucion(id, institucionId);

        return reactivarLegajo(legajo);
    }

    /**
     * Ejecuta la operaciÃ³n de reactivaciÃ³n.
     */
    private Legajo reactivarLegajo(Legajo legajo) {

        if (!"ARCHIVADO".equals(legajo.getEstado())) {
            throw new IllegalArgumentException(
                    "Solo se puede reactivar un legajo archivado");
        }

        legajo.setEstado("ACTIVO");

        return legajoRepository.save(legajo);
    }
    // COMPATIBILIDAD TEMPORAL CON EL CONTROLADOR ACTUAL
    // =========================================================

    public List<Legajo> listar() {
        return legajoRepository.findAll();
    }

    public Legajo buscarPorId(UUID id) {

        return legajoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Legajo no encontrado"));
    }

    public List<Legajo> listarPorEstado(String estado) {

        validarEstado(estado);

        return legajoRepository.findByEstado(estado);
    }

    @Transactional(readOnly = true)
    public List<Legajo> buscar(String titulo, UUID categoriaId) {

        if (titulo != null && titulo.isBlank()) {
            titulo = null;
        }

        return legajoRepository.buscarPorTituloYCategoria(
                titulo, categoriaId);
    }

    // =========================================================
    // VALIDACIONES Y AUDITORIA
    // =========================================================

    /**
     * Obtiene una categorÃ­a y verifica que se encuentre activa.
     */
    private Categoria obtenerCategoriaActiva(UUID categoriaId) {

        if (categoriaId == null) {
            throw new IllegalArgumentException(
                    "La categorÃ­a es obligatoria");
        }

        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new IllegalArgumentException("CategorÃ­a no encontrada"));

        if (!Boolean.TRUE.equals(categoria.getActiva())) {
            throw new IllegalArgumentException(
                    "No se puede utilizar una categorÃ­a inactiva");
        }

        return categoria;
    }

    /**
     * Verifica que se haya informado una instituciÃ³n.
     */
    private void validarInstitucion(UUID institucionId) {

        if (institucionId == null) {
            throw new IllegalArgumentException(
                    "La instituciÃ³n es obligatoria");
        }
    }

    /**
     * Valida el tÃ­tulo del legajo.
     */
    private void validarTitulo(String titulo) {

        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException(
                    "El tÃ­tulo es obligatorio");
        }

        if (titulo.trim().length() > 150) {
            throw new IllegalArgumentException(
                    "El tÃ­tulo no puede superar los 150 caracteres");
        }
    }

    /**
     * Valida la longitud de la descripciÃ³n.
     */
    private void validarDescripcion(String descripcion) {

        if (descripcion != null && descripcion.length() > 500) {
            throw new IllegalArgumentException(
                    "La descripciÃ³n no puede superar los 500 caracteres");
        }
    }

    /**
     * Valida los estados permitidos para un legajo.
     */
    private void validarEstado(String estado) {

        if (!"ACTIVO".equals(estado) && !"ARCHIVADO".equals(estado)) {
            throw new IllegalArgumentException(
                    "Estado de legajo invÃ¡lido");
        }
    }

    /**
     * Registra el evento de auditorÃ­a en el servicio Outbox.
     */
    private void registrarAuditoria(
            String accion,
            Legajo legajo,
            String operacion,
            String actor) {

        String payloadJson = """
                {
                  "eventType": "AUD-01",
                  "action": "%s",
                  "operation": "%s",
                  "aggregateId": "%s",
                  "titulo": "%s",
                  "actor": "%s"
                }
                """.formatted(
                accion,
                operacion,
                legajo.getId(),
                escaparJson(legajo.getTitulo()),
                escaparJson(actor));

        outboxEventService.registrarEvento(
                "AUD-01",
                "LEGAJO",
                legajo.getId(),
                payloadJson);
    }

    /**
     * Escapa caracteres especiales para construir un JSON vÃ¡lido.
     */
    private String escaparJson(String valor) {

        if (valor == null) {
            return "";
        }

        return valor
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // =========================================================
    // CONSULTA PAGINADA
    // =========================================================

    /**
     * Consulta legajos paginados y filtrados por instituciÃ³n,
     * tÃ­tulo, categorÃ­a y estado.
     *
     * Cada resultado se convierte a LegajoResponseDTO para
     * entregar categoriaId y categoriaNombre al consumidor.
     */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<LegajoResponseDTO> buscarPaginadoPorInstitucion(
            UUID institucionId,
            String titulo,
            UUID categoriaId,
            String estado,
            int page,
            int size) {

        validarInstitucion(institucionId);

        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "ParÃ¡metros de paginaciÃ³n invÃ¡lidos");
        }

        if (estado != null) {
            validarEstado(estado);
        }

        final String tituloFiltro = (titulo == null || titulo.isBlank())
                ? null
                : titulo.trim().toLowerCase();

        UUID idFiltro = null;
        if (titulo != null && !titulo.isBlank()) {
            try {
                idFiltro = UUID.fromString(titulo.trim());
            } catch (IllegalArgumentException ignored) {
                // La búsqueda no corresponde a un UUID.
            }
        }
        final UUID idFiltroFinal = idFiltro;

        final UUID categoriaFiltro = categoriaId;
        final String estadoFiltro = estado;

        org.springframework.data.jpa.domain.Specification<Legajo> spec = (root, query, cb) -> {

            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            predicates.add(
                    cb.equal(root.get("institucionId"), institucionId));

            if (tituloFiltro != null) {
                var tituloPredicate = cb.like(
                        cb.lower(root.<String>get("titulo")),
                        "%" + tituloFiltro + "%");

                if (idFiltroFinal != null) {
                    predicates.add(cb.or(
                            tituloPredicate,
                            cb.equal(root.get("id"), idFiltroFinal)));
                } else {
                    predicates.add(tituloPredicate);
                }
            }

            if (categoriaFiltro != null) {
                predicates.add(cb.equal(
                        root.get("categoria").get("id"),
                        categoriaFiltro));
            }

            if (estadoFiltro != null && !estadoFiltro.isBlank()) {
                predicates.add(
                        cb.equal(root.get("estado"), estadoFiltro));
            }

            return cb.and(
                    predicates.toArray(
                            new jakarta.persistence.criteria.Predicate[0]));
        };

        var pageable = org.springframework.data.domain.PageRequest.of(page, size);

        return legajoRepository.findAll(spec, pageable)
                .map(LegajoResponseDTO::new);
    }
}


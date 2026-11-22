
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
     * Lista los legajos pertenecientes a una institución.
     */
    @Transactional(readOnly = true)
    public List<Legajo> listarPorInstitucion(UUID institucionId) {
        validarInstitucion(institucionId);

        return legajoRepository.findAllByInstitucionIdOrderByCreatedAtDesc(
                institucionId);
    }

    /**
     * Lista los legajos de una institución filtrados por estado.
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
     * Busca un legajo por su identificador y la institución propietaria.
     *
     * Este método conserva la consulta original y se mantiene
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
     * La conversión a DTO ocurre dentro de la transacción activa.
     * Esto permite acceder a la categoría configurada como LAZY
     * y obtener categoriaId y categoriaNombre correctamente.
     *
     * Este método debe utilizarse en el endpoint de detalle.
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
     * Busca legajos por título y categoría dentro de una institución.
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
     * Crea un nuevo legajo asociado a una institución y categoría.
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
                        "El título debe ser un texto");
            }

            validarTitulo(titulo);
            legajo.setTitulo(titulo.trim());
        }

        if (cambios.containsKey("descripcion")) {
            Object valor = cambios.get("descripcion");

            if (valor != null && !(valor instanceof String)) {
                throw new IllegalArgumentException(
                        "La descripción debe ser un texto");
            }

            String descripcion = (String) valor;
            validarDescripcion(descripcion);
            legajo.setDescripcion(descripcion);
        }

        if (cambios.containsKey("categoriaId")) {
            Object valor = cambios.get("categoriaId");

            if (!(valor instanceof String categoriaTexto)) {
                throw new IllegalArgumentException(
                        "categoriaId debe ser un UUID válido");
            }

            UUID categoriaId;

            try {
                categoriaId = UUID.fromString(categoriaTexto);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(
                        "categoriaId debe ser un UUID válido");
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
     * Archiva un legajo validando su institución.
     */
    @Transactional
    public Legajo archivar(UUID id, UUID institucionId, String actor) {

        Legajo legajo = buscarPorIdEInstitucion(id, institucionId);

        return archivarLegajo(legajo);
    }

    /**
     * Ejecuta la operación de archivado.
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
     * Obtiene una categoría y verifica que se encuentre activa.
     */
    private Categoria obtenerCategoriaActiva(UUID categoriaId) {

        if (categoriaId == null) {
            throw new IllegalArgumentException(
                    "La categoría es obligatoria");
        }

        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        if (!Boolean.TRUE.equals(categoria.getActiva())) {
            throw new IllegalArgumentException(
                    "No se puede utilizar una categoría inactiva");
        }

        return categoria;
    }

    /**
     * Verifica que se haya informado una institución.
     */
    private void validarInstitucion(UUID institucionId) {

        if (institucionId == null) {
            throw new IllegalArgumentException(
                    "La institución es obligatoria");
        }
    }

    /**
     * Valida el título del legajo.
     */
    private void validarTitulo(String titulo) {

        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException(
                    "El título es obligatorio");
        }

        if (titulo.trim().length() > 150) {
            throw new IllegalArgumentException(
                    "El título no puede superar los 150 caracteres");
        }
    }

    /**
     * Valida la longitud de la descripción.
     */
    private void validarDescripcion(String descripcion) {

        if (descripcion != null && descripcion.length() > 500) {
            throw new IllegalArgumentException(
                    "La descripción no puede superar los 500 caracteres");
        }
    }

    /**
     * Valida los estados permitidos para un legajo.
     */
    private void validarEstado(String estado) {

        if (!"ACTIVO".equals(estado) && !"ARCHIVADO".equals(estado)) {
            throw new IllegalArgumentException(
                    "Estado de legajo inválido");
        }
    }

    /**
     * Registra el evento de auditoría en el servicio Outbox.
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
     * Escapa caracteres especiales para construir un JSON válido.
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
     * Consulta legajos paginados y filtrados por institución,
     * título, categoría y estado.
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
                    "Parámetros de paginación inválidos");
        }

        if (estado != null) {
            validarEstado(estado);
        }

        final String tituloFiltro = (titulo == null || titulo.isBlank())
                ? null
                : titulo.trim().toLowerCase();

        final UUID categoriaFiltro = categoriaId;
        final String estadoFiltro = estado;

        org.springframework.data.jpa.domain.Specification<Legajo> spec = (root, query, cb) -> {

            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            predicates.add(
                    cb.equal(root.get("institucionId"), institucionId));

            if (tituloFiltro != null) {
                predicates.add(cb.like(
                        cb.lower(root.<String>get("titulo")),
                        "%" + tituloFiltro + "%"));
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

package cl.legga.legajos.exception;

/**
 * Excepción utilizada cuando un recurso solicitado
 * no existe en el sistema.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
package pt.app.pgmr.api.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import pt.app.pgmr.application.exception.DomainValidationException;
import pt.app.pgmr.application.exception.ResourceNotFoundException;

import java.time.OffsetDateTime;
import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    /**
     * Handles validation errors for request bodies annotated with @Valid.
     *
     * @param ex the exception
     * @return the error response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex
    ) {
        List<String> messages = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", messages);
    }

    /**
     * Handles validation errors for request parameters annotated with @Valid.
     *
     * @param ex the exception
     * @return the error response
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", List.of(ex.getMessage()));
    }

    /**
     * Handles type mismatch errors for request parameters.
     *
     * @param ex the exception
     * @return the error response
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "Invalid value for parameter '%s': '%s'".formatted(
                ex.getName(),
                ex.getValue()
        );
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid request", List.of(message));
    }

    /**
     * Handles unreadable JSON bodies.
     *
     * @param ex the exception
     * @return the error response
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex) {
        log.warn("Malformed JSON request", ex);
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Malformed JSON request",
                List.of("The request body could not be parsed.")
        );
    }

    /**
     * Handles resource not found errors.
     *
     * @param ex the exception
     * @return the error response
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Resource not found", List.of(ex.getMessage()));
    }

    /**
     * Handles domain validation errors.
     *
     * @param ex the exception
     * @return the error response
     */
    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleDomainValidation(DomainValidationException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Domain validation failed", List.of(ex.getMessage()));
    }

    /**
     * Handles data integrity violation errors.
     *
     * @param ex the exception
     * @return the error response
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.error("Database constraint violation", ex);
        return buildResponse(
                HttpStatus.CONFLICT,
                "Database constraint violation",
                List.of("A database constraint was violated while processing the request.")
        );
    }

    /**
     * Handles illegal argument exceptions.
     *
     * @param ex the exception
     * @return the error response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid operation", List.of(ex.getMessage()));
    }

    /**
     * Handles all other exceptions.
     *
     * @param ex the exception
     * @return the error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex) {
        log.error("Unhandled exception", ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected error",
                List.of("An unexpected error occurred while processing the request.")
        );
    }

    /**
     * Builds a response entity with the specified status, error, and messages.
     *
     * @param status   2xx or 4xx
     * @param error    error message
     * @param messages error messages
     * @return the response entity
     */
    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String error,
            List<String> messages
    ) {
        return ResponseEntity.status(status).body(
                new ApiErrorResponse(
                        OffsetDateTime.now(),
                        status.value(),
                        error,
                        messages
                )
        );
    }
}

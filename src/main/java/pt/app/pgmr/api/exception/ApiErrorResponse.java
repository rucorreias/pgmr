package pt.app.pgmr.api.exception;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * API error response DTO.
 *
 * @param timestamp
 * @param status
 * @param error
 * @param messages
 */
public record ApiErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        List<String> messages
) {
}

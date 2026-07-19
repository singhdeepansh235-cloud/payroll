package com.srmcem.payroll.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Generic wrapper for all API responses.
 *
 * <p>Every endpoint returns this envelope so clients always deal with the same
 * JSON shape, regardless of success or failure.
 *
 * <pre>
 * {
 *   "success": true,
 *   "message": "Employee created successfully.",
 *   "data": { ... },       // present only on success
 *   "timestamp": "..."
 * }
 * </pre>
 *
 * @param <T> the type of the payload carried in {@code data}
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final LocalDateTime timestamp;

    // -----------------------------------------------------------------------
    // Private constructor — use static factories below
    // -----------------------------------------------------------------------

    private ApiResponse(boolean success, String message, T data) {
        this.success   = success;
        this.message   = message;
        this.data      = data;
        this.timestamp = LocalDateTime.now();
    }

    // -----------------------------------------------------------------------
    // Success factories
    // -----------------------------------------------------------------------

    /** 200/201 with a payload and a custom message. */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /** 200/201 with a payload and a default "Success." message. */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success.", data);
    }

    /** 204-style: success with a message but no body (e.g. delete). */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    // -----------------------------------------------------------------------
    // Error factory
    // -----------------------------------------------------------------------

    /** 4xx/5xx: failure with a message and no payload. */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}

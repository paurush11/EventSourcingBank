package dev.codescreen.exception;

import dev.codescreen.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleServerError(ResponseStatusException e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getReason(), String.valueOf(e.getStatusCode().value()));
        return ResponseEntity.status(e.getStatusCode()).body(errorResponse);
    }
}

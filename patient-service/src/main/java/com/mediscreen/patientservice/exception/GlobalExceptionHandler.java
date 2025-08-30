package com.mediscreen.patientservice.exception;

import com.mediscreen.patientservice.dto.ApiErrorResponse;
import com.mediscreen.patientservice.dto.ApiFieldError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<ApiFieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toApiFieldError)
                .toList();

        ApiErrorResponse body = new ApiErrorResponse("Validation error", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<ApiFieldError> fieldErrors = ex.getConstraintViolations()
                .stream()
                .map(this::toApiFieldError)
                .toList();
        ApiErrorResponse body = new ApiErrorResponse("Validation error", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(PatientNotFoundException ex) {
        ApiErrorResponse body = new ApiErrorResponse(
                ex.getMessage() != null ? ex.getMessage() : "Resource not found",
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {
        ApiErrorResponse body = new ApiErrorResponse("Erreur interne", null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private ApiFieldError toApiFieldError(FieldError fe) {
        return new ApiFieldError(fe.getField(), fe.getDefaultMessage());
    }

    private ApiFieldError toApiFieldError(ConstraintViolation<?> cv) {
        String field = cv.getPropertyPath() != null ? cv.getPropertyPath().toString() : null;
        return new ApiFieldError(field, cv.getMessage());
    }
}

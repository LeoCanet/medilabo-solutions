package com.mediscreen.frontend.exception;

/**
 * Exception métier pour les erreurs d'évaluation du risque diabète
 * Encapsule les erreurs techniques du assessment-service (500+, timeout, etc.)
 */
public class AssessmentServiceException extends RuntimeException {

    private final int statusCode;

    public AssessmentServiceException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}

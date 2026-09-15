package com.mlcdev.realestate.exception.handler;

import com.mlcdev.realestate.exception.*;
import com.mlcdev.realestate.exception.response.ApiError;
import com.mlcdev.realestate.exception.response.ValidationApiError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@NullMarked
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {



    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    @Value("${spring.servlet.multipart.max-request-size}")
    private String maxRequestSize;


    // spring mvc handler overriding

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ValidationApiError error = new ValidationApiError(status.value(), "Validation failed", requestUri(request));
        ex.getBindingResult().getFieldErrors().forEach(fieldError -> error.addError(fieldError.getField(), fieldError.getDefaultMessage()));
        return ResponseEntity.badRequest().body(error);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ApiError error =
                new ApiError(status.value(),
                        "Uploaded file or request exceeds the maximum allowed size. Max file size: " + maxFileSize + ". Max request size: " + maxRequestSize,
                        requestUri(request));
        return ResponseEntity.status(status).body(error);
    }


    @Override
    protected @Nullable ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ApiError error = new ApiError(status.value(),
                "Invalid value for request parameter",
                requestUri(request));
        return ResponseEntity.status(status).body(error);

    }

    @Override
    protected @Nullable ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ApiError error = new ApiError(status.value(),
                "Malformed or invalid request body",
                requestUri(request));
        return ResponseEntity.status(status).body(error);
    }

    private String requestUri(WebRequest request) {
        return ((ServletWebRequest) request).getRequest().getRequestURI();
    }




    // custom exceptions handlers

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ApiError error = new ApiError(status.value(), exception.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ApiError error = new ApiError(status.value(), exception.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiError> handleBusinessRule(BusinessRuleException exception, HttpServletRequest request) {
        ApiError error = new ApiError(422, exception.getMessage(), request.getRequestURI());
        return ResponseEntity.status(422).body(error);
    }

    @ExceptionHandler(ResourceMismatchException.class)
    public ResponseEntity<ApiError> handleMismatch(ResourceMismatchException exception, HttpServletRequest request) {
        ApiError error = new ApiError(422, exception.getMessage(), request.getRequestURI());
        return ResponseEntity.status(422).body(error);
    }

    @ExceptionHandler(EmptyResourceException.class)
    public ResponseEntity<ApiError> handleEmptyResource(EmptyResourceException exception, HttpServletRequest request) {
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST.value(), exception.getMessage(), request.getRequestURI());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ApiError> handleFileStorage(FileStorageException exception, HttpServletRequest request) {
        log.error("File storage error on {}: {}", request.getRequestURI(), exception.getMessage(), exception);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ApiError error = new ApiError(status.value(), exception.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ApiError> handlePropertyReference(
            HttpServletRequest request
    ) {
        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid sort property",
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(
            HttpServletRequest request
    ) {
        ApiError error = new ApiError(
                HttpStatus.CONFLICT.value(),
                "Request conflicts with existing data or database constraints",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccess(HttpServletRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        ApiError error = new ApiError(status.value(), "Access Denied", request.getRequestURI());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiError> handleAuthorization(HttpServletRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        ApiError error = new ApiError(status.value(), "Insufficient Permission", request.getRequestURI());
        return ResponseEntity.status(status).body(error);
    }

    // fallback exception handler

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("Unexpected error on {}: {}", request.getRequestURI(), exception.getMessage(), exception);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ApiError error = new ApiError(status.value(), "An unexpected error occurred", request.getRequestURI());
        return ResponseEntity.status(status).body(error);
    }


}

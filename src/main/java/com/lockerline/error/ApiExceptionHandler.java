package com.lockerline.error;

import java.net.URI;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.lockerline.config.RequestIdFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(ApiExceptionHandler.class);

    private ResponseEntity<ProblemDetail> response(ProblemDetail problem) {
        return ResponseEntity
                .status(problem.getStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    private ProblemDetail problem(
            HttpStatus status,
            String type,
            String title,
            String detail,
            HttpServletRequest request) {

        ProblemDetail result =
                ProblemDetail.forStatusAndDetail(status, detail);

        result.setType(URI.create(type));
        result.setTitle(title);
        result.setInstance(URI.create(request.getRequestURI()));

        return result;
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ProblemDetail> refusal(
            DomainException e,
            HttpServletRequest request) {

        ProblemDetail result = problem(
                HttpStatus.valueOf(e.status()),
                e.type(),
                e.title(),
                e.getMessage(),
                request);

        for (Map.Entry<String, Object> entry : e.properties().entrySet()) {
            result.setProperty(entry.getKey(), entry.getValue());
        }

        return response(result);
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentNotValidException.class,
            HandlerMethodValidationException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<ProblemDetail> validation(
            Exception e,
            HttpServletRequest request) {

        ProblemDetail result = problem(
                HttpStatus.BAD_REQUEST,
                "/errors/validation",
                "Invalid request",
                "The request is not valid",
                request);

        if (e instanceof MethodArgumentTypeMismatchException mismatch) {
            result.setProperty(
                    "errors",
                    new Object[] {
                            Map.of(
                                    "field", mismatch.getName(),
                                    "message", "This value is not allowed")
                    });

        } else if (e instanceof MethodArgumentNotValidException invalid) {
            result.setProperty(
                    "errors",
                    invalid.getBindingResult().getFieldErrors().stream()
                            .map(error -> Map.of(
                                    "field", error.getField(),
                                    "message",
                                    error.getDefaultMessage() == null
                                            ? "This value is not valid"
                                            : error.getDefaultMessage()))
                            .toList());

        } else if (e instanceof HttpMessageNotReadableException readable) {
            String field = "body";
            String message = "This request body cannot be read";

            Throwable cause = readable.getCause();

            if (cause instanceof JsonMappingException mapping
                    && !mapping.getPath().isEmpty()) {
                JsonMappingException.Reference reference =
                        mapping.getPath().get(mapping.getPath().size() - 1);

                if (reference.getFieldName() != null) {
                    field = reference.getFieldName();
                }

                message = "This value cannot be read";
            }

            result.setProperty(
                    "errors",
                    new Object[] {
                            Map.of("field", field, "message", message)
                    });

        } else if (e instanceof HandlerMethodValidationException) {
            result.setProperty(
                    "errors",
                    new Object[] {
                            Map.of(
                                    "field", "request",
                                    "message", "A request parameter is not valid")
                    });

        } else if (e instanceof ConstraintViolationException violation) {
            result.setProperty(
                    "errors",
                    violation.getConstraintViolations().stream()
                            .map(v -> Map.of(
                                    "field", v.getPropertyPath().toString(),
                                    "message", v.getMessage()))
                            .toList());
        }

        return response(result);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> methodNotAllowed(
            HttpRequestMethodNotSupportedException e,
            HttpServletRequest request) {

        ProblemDetail result = problem(
                HttpStatus.METHOD_NOT_ALLOWED,
                "/errors/method-not-allowed",
                "Method not allowed",
                "This HTTP method is not allowed for this endpoint",
                request);

        return response(result);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> unexpected(
            Exception e,
            HttpServletRequest request) {

        String requestId = MDC.get(RequestIdFilter.KEY);

        log.error("unexpected error requestId={}", requestId, e);

        ProblemDetail result = problem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "/errors/internal",
                "Internal error",
                "Something went wrong on our side; quote the request id if you report it",
                request);

        result.setProperty("requestId", requestId);

        return response(result);
    }
}

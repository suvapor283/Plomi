package com.ksj.plomi.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDto> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("BusinessException 발생: {}", e.getMessage());

        ErrorCode errorCode = e.getErrorCode();

        ErrorResponseDto errorResponseDto = ErrorResponseDto.of(
                errorCode.getHttpStatus(),
                errorCode,
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponseDto, errorCode.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.warn("MethodArgumentNotValidException 발생: {}", e.getMessage());

        BindingResult bindingResult = e.getBindingResult();
        List<ErrorResponseDto.ValidationError> validationErrors = bindingResult.getFieldErrors().stream()
                .map(error -> ErrorResponseDto.ValidationError.builder()
                        .field(error.getField())
                        .defaultMessage(error.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());

        ErrorResponseDto errorResponseDto = ErrorResponseDto.of(
                HttpStatus.BAD_REQUEST,
                ErrorCode.INVALID_INPUT_VALUE,
                request.getRequestURI(),
                validationErrors
        );

        return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDto> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.warn("HttpRequestMethodNotSupportedException 발생: {} - 요청 URI / 지원하는 메서드: {}",
                e.getMessage(),
                request.getRequestURI(),
                Objects.requireNonNullElse(e.getSupportedHttpMethods(), HttpMethod.GET).toString()
        );

        ErrorResponseDto errorResponseDto = ErrorResponseDto.of(
                ErrorCode.METHOD_NOT_ALLOWED.getHttpStatus(),
                ErrorCode.METHOD_NOT_ALLOWED,
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponseDto, ErrorCode.METHOD_NOT_ALLOWED.getHttpStatus());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNoHandlerFound(NoHandlerFoundException e, HttpServletRequest request) {
        log.warn("NoHandlerFoundException 발생: {} - 요청 URI: {}", e.getMessage(), request.getRequestURI());

        ErrorResponseDto errorResponseDto = ErrorResponseDto.of(
                ErrorCode.NOT_FOUND.getHttpStatus(),
                ErrorCode.NOT_FOUND,
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponseDto, ErrorCode.NOT_FOUND.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleAllUncaughtException(Exception e, HttpServletRequest request) {
        log.error("예상치 못한 최상위 예외 발생: {}", e.getMessage(), e);

        ErrorResponseDto errorResponseDto = ErrorResponseDto.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR,
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponseDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

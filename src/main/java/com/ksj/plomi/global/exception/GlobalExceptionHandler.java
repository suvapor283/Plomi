package com.ksj.plomi.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
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
}

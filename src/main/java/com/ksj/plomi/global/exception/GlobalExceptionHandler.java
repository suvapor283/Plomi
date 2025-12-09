package com.ksj.plomi.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
}

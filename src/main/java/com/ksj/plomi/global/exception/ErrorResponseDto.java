package com.ksj.plomi.global.exception;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class ErrorResponseDto {

    private LocalDateTime timestamp;
    private int status;
    private String code;
    private String name;
    private String message;
    private String path;
    private List<ValidationError> errors;

    @Builder
    public ErrorResponseDto(HttpStatus httpStatus, String code, String name, String message, String path, List<ValidationError> errors) {
        this.timestamp = LocalDateTime.now();
        this.status = httpStatus.value();
        this.code = code;
        this.name = name;
        this.message = message;
        this.path = path;
        this.errors = errors;
    }

    public static ErrorResponseDto of(final HttpStatus httpStatus, final ErrorCode errorCode, final String path) {
        return ErrorResponseDto.builder()
                .httpStatus(httpStatus)
                .code(errorCode.getCode())
                .name(errorCode.name())
                .message(errorCode.getMessage())
                .path(path)
                .build();
    }

    public static ErrorResponseDto of(final HttpStatus httpStatus, final ErrorCode errorCode, final String path, final List<ValidationError> errors) {
        return ErrorResponseDto.builder()
                .httpStatus(httpStatus)
                .code(errorCode.getCode())
                .name(errorCode.name())
                .message(errorCode.getMessage())
                .path(path)
                .errors(errors)
                .build();
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ValidationError {

        private String field;
        private String defaultMessage;

        @Builder
        public ValidationError(String field, String defaultMessage) {
            this.field = field;
            this.defaultMessage = defaultMessage;
        }
    }
}

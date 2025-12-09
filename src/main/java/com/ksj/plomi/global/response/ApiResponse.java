package com.ksj.plomi.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String code;
    private final String message;
    private final T data;

    public static <T> ApiResponse<T> success(SuccessStatus status, String message, T data) {
        return new ApiResponse<>(
                true,
                status.getCode(),
                message,
                data
        );
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                true,
                SuccessStatus.SUCCESS.getCode(),
                null,
                data
        );
    }
}

package com.ksj.plomi.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    //Common Errors (C0XX)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "유효하지 않은 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "지원하지 않는 HTTP Method 입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 내부 오류가 발생했습니다. 문제가 계속될 경우 관리자에게 문의하세요."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "C004", "요청하신 리소스를 찾을 수 없습니다."),

    // Auth Errors (A0XX)
    USERNAME_DUPLICATION(HttpStatus.CONFLICT, "A001", "이미 존재하는 아이디입니다."),
    EMAIL_DUPLICATION(HttpStatus.CONFLICT, "A002", "이미 등록된 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A003", "아이디 또는 비밀번호가 일치하지 않습니다."),
    UNAUTHORIZED_TOKEN(HttpStatus.UNAUTHORIZED, "A004", "인증 토큰이 유효하지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "A005", "사용자를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

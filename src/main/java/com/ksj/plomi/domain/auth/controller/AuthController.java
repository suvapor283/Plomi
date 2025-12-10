package com.ksj.plomi.domain.auth.controller;

import com.ksj.plomi.domain.auth.dto.LoginRequestDto;
import com.ksj.plomi.domain.auth.dto.LoginResponseDto;
import com.ksj.plomi.domain.auth.dto.SignupRequestDto;
import com.ksj.plomi.domain.auth.dto.SignupResponseDto;
import com.ksj.plomi.domain.auth.service.AuthService;
import com.ksj.plomi.global.response.ApiResponse;
import com.ksj.plomi.global.response.SuccessStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponseDto>> signup(@Valid @RequestBody SignupRequestDto requestDto) {
        SignupResponseDto responseDto = authService.signup(requestDto);

        ApiResponse<SignupResponseDto> body =
                ApiResponse.success(SuccessStatus.CREATED, null, responseDto);

        return ResponseEntity
                .status(SuccessStatus.CREATED.getHttpStatus())
                .body(body);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto requestDto) {
        LoginResponseDto responseDto = authService.login(requestDto);

        ApiResponse<LoginResponseDto> body =
                ApiResponse.success(responseDto);

        return ResponseEntity
                .status(SuccessStatus.SUCCESS.getHttpStatus())
                .body(body);
    }
}

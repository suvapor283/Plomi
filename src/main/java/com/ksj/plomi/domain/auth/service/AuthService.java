package com.ksj.plomi.domain.auth.service;

import com.ksj.plomi.domain.auth.dto.LoginRequestDto;
import com.ksj.plomi.domain.auth.dto.LoginResponseDto;
import com.ksj.plomi.domain.auth.dto.SignupRequestDto;
import com.ksj.plomi.domain.auth.dto.SignupResponseDto;
import com.ksj.plomi.domain.users.repository.UserRepository;
import com.ksj.plomi.domain.users.entity.User;
import com.ksj.plomi.domain.users.role.UserRole;
import com.ksj.plomi.global.exception.BusinessException;
import com.ksj.plomi.global.exception.ErrorCode;
import com.ksj.plomi.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public SignupResponseDto signup(SignupRequestDto requestDto) {
        validateSignUpRequest(requestDto);

        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        User newUser = createUserEntity(requestDto, encodedPassword);
        User savedUser = userRepository.save(newUser);

        return SignupResponseDto.from(savedUser);
    }

    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequestDto requestDto) {
        User user = findUserByUsername(requestDto.getUsername());

        validatePassword(requestDto.getPassword(), user.getPassword());

        String accessToken = jwtTokenProvider.createAccessToken(user.getUsername());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUsername());

        saveRefreshToken(user.getId(), refreshToken);

        return LoginResponseDto.from(accessToken, refreshToken, user);
    }

    // ===============================================================================================
    // --- 회원가입(Signup) 관련 보조 로직 ---
    private void validateSignUpRequest(SignupRequestDto requestDto) {
        checkUsernameDuplicate(requestDto.getUsername());
        checkEmailDuplicate(requestDto.getEmail());
    }

    private void checkUsernameDuplicate(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.USERNAME_DUPLICATION);
        }
    }

    private void checkEmailDuplicate(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATION);
        }
    }

    private User createUserEntity(SignupRequestDto requestDto, String encodedPassword) {
        return User.builder()
                .username(requestDto.getUsername())
                .password(encodedPassword)
                .email(requestDto.getEmail())
                .name(requestDto.getName())
                .role(UserRole.USER)
                .build();
    }

    // ===============================================================================================
    // --- 로그인(Login) 관련 보조 로직 ---
    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    @Transactional
    protected void saveRefreshToken(Long userId, String refreshToken) {
        String key = buildRefreshTokenKey(userId);
        long ttl = jwtTokenProvider.getRefreshTokenValidityInMillis();

        redisTemplate.opsForValue()
                .set(key, refreshToken, ttl, TimeUnit.MILLISECONDS);
    }

    private String buildRefreshTokenKey(Long userId) {
        return "RT:" + userId;
    }
}

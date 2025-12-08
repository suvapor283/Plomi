package com.ksj.plomi.domain.auth.service;

import com.ksj.plomi.domain.auth.dto.SignupRequestDto;
import com.ksj.plomi.domain.users.repository.UserRepository;
import com.ksj.plomi.domain.users.entity.User;
import com.ksj.plomi.domain.users.role.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User signup(SignupRequestDto requestDto) {
        validateSignUpRequest(requestDto);

        String encodedPassword = encodePassword(requestDto.getPassword());

        User newUser = createUserEntity(requestDto, encodedPassword);

        return userRepository.save(newUser);
    }

    private void validateSignUpRequest(SignupRequestDto requestDto) {
        checkUsernameDuplicate(requestDto.getUsername());
        checkEmailDuplicate(requestDto.getEmail());
    }

    private void checkUsernameDuplicate(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 존재하는 아이디입니다.");
        }
    }

    private void checkEmailDuplicate(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 등록된 이메일입니다.");
        }
    }

    private String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
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
}

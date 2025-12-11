package com.ksj.plomi.domain.auth.dto;

import com.ksj.plomi.domain.users.entity.User;
import com.ksj.plomi.domain.users.role.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class LoginResponseDto {

    private final String accessToken;
    private final String tokenType;
    private final String refreshToken;
    private final Long id;
    private final String name;
    private final UserRole role;
    private final String profileImageUrl;
    private final String statusMessage;

    public static LoginResponseDto from(String accessToken, String refreshToken, User user) {
        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .refreshToken(refreshToken)
                .id(user.getId())
                .name(user.getName())
                .role(user.getRole())
                .profileImageUrl(user.getProfileImageUrl())
                .statusMessage(user.getStatusMessage())
                .build();
    }
}

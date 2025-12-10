package com.ksj.plomi.domain.auth.dto;

import com.ksj.plomi.domain.users.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class SignupResponseDto {

    private final String username;
    private final String name;

    public static SignupResponseDto from(User user) {
        return SignupResponseDto.builder()
                .username(user.getUsername())
                .name(user.getName())
                .build();
    }
}

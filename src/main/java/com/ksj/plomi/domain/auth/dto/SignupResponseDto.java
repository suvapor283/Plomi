package com.ksj.plomi.domain.auth.dto;

import com.ksj.plomi.domain.users.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupResponseDto {

    private String username;
    private String name;

    public static SignupResponseDto from(User user) {
        return SignupResponseDto.builder()
                .username(user.getUsername())
                .name(user.getName())
                .build();
    }
}

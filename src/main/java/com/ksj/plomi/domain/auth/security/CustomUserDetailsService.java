package com.ksj.plomi.domain.auth.security;

import com.ksj.plomi.domain.users.entity.User;
import com.ksj.plomi.domain.users.repository.UserRepository;
import com.ksj.plomi.global.exception.BusinessException;
import com.ksj.plomi.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        return new CustomUserDetails(user);
    }
}

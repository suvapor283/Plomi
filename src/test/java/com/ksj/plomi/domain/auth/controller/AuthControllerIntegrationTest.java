package com.ksj.plomi.domain.auth.controller;

import com.ksj.plomi.domain.auth.dto.SignupRequestDto;
import com.ksj.plomi.domain.users.entity.User;
import com.ksj.plomi.domain.users.repository.UserRepository;
import com.ksj.plomi.domain.users.role.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원가입 성공 테스트: 201 Created & 저장된 유저 반환")
    void signup_success() throws Exception {
        SignupRequestDto requestDto = new SignupRequestDto();
        requestDto.setUsername("testuser");
        requestDto.setPassword("Password1!");
        requestDto.setPassword2("Password1!");
        requestDto.setEmail("test@example.com");
        requestDto.setName("testuserone");

        String json = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(
                        post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).isEqualTo("회원가입 성공!");
        assertThat(userRepository.count()).isEqualTo(1);

        User savedUser = userRepository.findByUsername("testuser")
                .orElseThrow(() -> new AssertionError("유저가 DB에 저장되지 않았습니다."));

        assertThat(passwordEncoder.matches("Password1!", savedUser.getPassword())).isTrue();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getName()).isEqualTo("testuserone");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("비밀번호 불일치 테스트: 400 Bad Request & INVALID_INPUT_VALUE 코드 반환")
    void signup_fail_when_password_not_match() throws Exception {
        SignupRequestDto requestDto = new SignupRequestDto();
        requestDto.setUsername("testuser2");
        requestDto.setPassword("Password2!");
        requestDto.setPassword2("Different2!");
        requestDto.setEmail("test2@example.com");
        requestDto.setName("testusertwo");

        String json = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(
                        post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"))
                .andExpect(jsonPath("$.name").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 입력값입니다."))
                .andExpect(jsonPath("$.errors[0].field").value("password2"))
                .andExpect(jsonPath("$.errors[0].defaultMessage").value("비밀번호와 일치하지 않습니다."));

        assertThat(userRepository.existsByUsername("testuser2")).isFalse();
    }

    @Test
    @DisplayName("이메일 중복 테스트: 409 Conflict & EMAIL_DUPLICATION 코드 반환")
    void signup_fail_when_email_duplicate() throws Exception {
        User existing = User.builder()
                .username("existingUser")
                .password(passwordEncoder.encode("Password3!"))
                .email("dup@example.com")
                .name("testuserthree")
                .role(UserRole.USER)
                .build();

        userRepository.save(existing);

        SignupRequestDto requestDto = new SignupRequestDto();
        requestDto.setUsername("newUser");
        requestDto.setPassword("Password3!");
        requestDto.setPassword2("Password3!");
        requestDto.setEmail("dup@example.com");
        requestDto.setName("testuserfour");

        String json = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(
                        post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("A002"))
                .andExpect(jsonPath("$.name").value("EMAIL_DUPLICATION"))
                .andExpect(jsonPath("$.message").value("이미 등록된 이메일입니다."));

        assertThat(userRepository.existsByUsername("newUser")).isFalse();
    }
}

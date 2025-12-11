package com.ksj.plomi.domain.auth.controller;

import com.ksj.plomi.domain.auth.dto.LoginRequestDto;
import com.ksj.plomi.domain.auth.dto.SignupRequestDto;
import com.ksj.plomi.domain.auth.dto.TokenRefreshRequestDto;
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
import tools.jackson.databind.JsonNode;
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
    @DisplayName("회원가입 성공: 201 Created — 유저 저장 성공")
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
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("CREATED"))
                .andExpect(jsonPath("$.message").doesNotExist())
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.name").value("testuserone"))
                .andReturn();

        assertThat(userRepository.count()).isEqualTo(1);

        User savedUser = userRepository.findByUsername("testuser")
                .orElseThrow(() -> new AssertionError("유저가 DB에 저장되지 않았습니다."));

        assertThat(passwordEncoder.matches("Password1!", savedUser.getPassword())).isTrue();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getName()).isEqualTo("testuserone");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("회원가입 실패: 비밀번호 불일치 → 400 Bad Request INVALID_INPUT_VALUE")
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
    @DisplayName("회원가입 실패: 이메일 중복 → 409 Conflict EMAIL_DUPLICATION")
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

    @Test
    @DisplayName("로그인 성공: 200 OK — AccessToken 및 RefreshToken 발급")
    void login_success() throws Exception {
        User user = User.builder()
                .username("loginUser")
                .password(passwordEncoder.encode("ValidPassword1!"))
                .email("login@example.com")
                .name("로그인유저")
                .role(UserRole.USER)
                .build();
        userRepository.save(user);

        LoginRequestDto requestDto = new LoginRequestDto();
        requestDto.setUsername("loginUser");
        requestDto.setPassword("ValidPassword1!");

        String json = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.name").value("로그인유저"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(responseBody);

        String accessToken = root.path("data").path("accessToken").asText();
        String refreshToken = root.path("data").path("refreshToken").asText();

        assertThat(accessToken).isNotBlank();
        assertThat(refreshToken).isNotBlank();
    }

    @Test
    @DisplayName("로그인 실패: 존재하지 않는 username → 404 Not Found USER_NOT_FOUND")
    void login_fail_user_not_found() throws Exception {
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("nonExistingUser");
        request.setPassword("Password1!");

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("A004"))
                .andExpect(jsonPath("$.name").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("로그인 실패: 비밀번호 불일치 → 401 Unauthorized INVALID_CREDENTIALS")
    void login_fail_wrong_password() throws Exception {
        User user = User.builder()
                .username("loginFailUser")
                .password(passwordEncoder.encode("ValidPassword1!"))
                .email("loginfail@example.com")
                .name("로그인실패유저")
                .role(UserRole.USER)
                .build();
        userRepository.save(user);

        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("loginFailUser");
        request.setPassword("WrongPassword!");

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A003"))
                .andExpect(jsonPath("$.name").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("토큰 재발급 성공: 200 OK — 새로운 AccessToken 및 RefreshToken 발급")
    void refresh_success() throws Exception {
        User user = User.builder()
                .username("refreshUser")
                .password(passwordEncoder.encode("ValidPassword1!"))
                .email("refresh@example.com")
                .name("리프레시유저")
                .role(UserRole.USER)
                .build();
        userRepository.save(user);

        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setUsername("refreshUser");
        loginRequest.setPassword("ValidPassword1!");

        String loginJson = objectMapper.writeValueAsString(loginRequest);

        MvcResult loginResult = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJson)
                )
                .andExpect(status().isOk())
                .andReturn();

        String loginBody = loginResult.getResponse().getContentAsString();
        JsonNode loginRoot = objectMapper.readTree(loginBody);

        String oldAccessToken = loginRoot.path("data").path("accessToken").asText();
        String oldRefreshToken = loginRoot.path("data").path("refreshToken").asText();

        TokenRefreshRequestDto refreshRequest = new TokenRefreshRequestDto();
        refreshRequest.setRefreshToken(oldRefreshToken);

        String refreshJson = objectMapper.writeValueAsString(refreshRequest);

        MvcResult refreshResult = mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(refreshJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andReturn();

        String refreshBody = refreshResult.getResponse().getContentAsString();
        JsonNode refreshRoot = objectMapper.readTree(refreshBody);

        String newAccessToken = refreshRoot.path("data").path("accessToken").asText();
        String newRefreshToken = refreshRoot.path("data").path("refreshToken").asText();

        assertThat(newAccessToken).isNotBlank();
        assertThat(newRefreshToken).isNotBlank();
        assertThat(newAccessToken).isNotEqualTo(oldAccessToken);
        assertThat(newRefreshToken).isNotEqualTo(oldRefreshToken);
    }

    @Test
    @DisplayName("토큰 재발급 실패: 잘못된 RefreshToken → 401 Unauthorized UNAUTHORIZED_TOKEN")
    void refresh_fail_invalid_token_string() throws Exception {
        TokenRefreshRequestDto request = new TokenRefreshRequestDto();
        request.setRefreshToken("this-is-not-a-valid-refresh-token");

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A005"))
                .andExpect(jsonPath("$.name").value("UNAUTHORIZED_TOKEN"))
                .andExpect(jsonPath("$.message").value("인증 정보가 유효하지 않습니다."));
    }

    @Test
    @DisplayName("토큰 재발급 실패: 회전 이후 이전 RefreshToken 사용 → 401 Unauthorized UNAUTHORIZED_TOKEN")
    void refresh_fail_old_refresh_token_after_rotation() throws Exception {
        User user = User.builder()
                .username("rotationUser")
                .password(passwordEncoder.encode("ValidPassword1!"))
                .email("rotation@example.com")
                .name("회전유저")
                .role(UserRole.USER)
                .build();
        userRepository.save(user);

        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setUsername("rotationUser");
        loginRequest.setPassword("ValidPassword1!");

        String loginJson = objectMapper.writeValueAsString(loginRequest);

        MvcResult loginResult = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJson)
                )
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginNode = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String oldRefreshToken = loginNode.path("data").path("refreshToken").asText();

        TokenRefreshRequestDto firstRefreshRequest = new TokenRefreshRequestDto();
        firstRefreshRequest.setRefreshToken(oldRefreshToken);

        String firstRefreshJson = objectMapper.writeValueAsString(firstRefreshRequest);

        MvcResult firstRefreshResult = mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(firstRefreshJson)
                )
                .andExpect(status().isOk())
                .andReturn();

        JsonNode firstRefreshNode = objectMapper.readTree(firstRefreshResult.getResponse().getContentAsString());
        String newRefreshToken = firstRefreshNode.path("data").path("refreshToken").asText();

        assertThat(newRefreshToken).isNotEqualTo(oldRefreshToken);

        TokenRefreshRequestDto secondRefreshRequest = new TokenRefreshRequestDto();
        secondRefreshRequest.setRefreshToken(oldRefreshToken);

        String secondJson = objectMapper.writeValueAsString(secondRefreshRequest);

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(secondJson)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A005"))
                .andExpect(jsonPath("$.name").value("UNAUTHORIZED_TOKEN"))
                .andExpect(jsonPath("$.message").value("인증 정보가 유효하지 않습니다."));
    }

    @Test
    @DisplayName("토큰 재발급 실패: Redis에 RefreshToken 없음 → 401 Unauthorized UNAUTHORIZED_TOKEN")
    void refresh_fail_no_refresh_in_redis() throws Exception {
        User user = User.builder()
                .username("redisNullUser")
                .password(passwordEncoder.encode("ValidPassword1!"))
                .email("nullredis@example.com")
                .name("레디스널유저")
                .role(UserRole.USER)
                .build();
        userRepository.save(user);

        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setUsername("redisNullUser");
        loginRequest.setPassword("ValidPassword1!");

        String loginJson = objectMapper.writeValueAsString(loginRequest);

        MvcResult loginResult = mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson)
        ).andExpect(status().isOk()).andReturn();

        JsonNode loginNode = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String refreshToken = loginNode.path("data").path("refreshToken").asText();

        String key = "RT:" + user.getId();

        TokenRefreshRequestDto refreshRequest = new TokenRefreshRequestDto();
        refreshRequest.setRefreshToken(refreshToken);

        String refreshJson = objectMapper.writeValueAsString(refreshRequest);

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(refreshJson)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A005"))
                .andExpect(jsonPath("$.name").value("UNAUTHORIZED_TOKEN"))
                .andExpect(jsonPath("$.message").value("인증 정보가 유효하지 않습니다."));
    }
}

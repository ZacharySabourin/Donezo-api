package io.github.zacharysabourin.donezo_api.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.client.RestTestClient;

import io.github.zacharysabourin.donezo_api.config.EmbeddedPostgresWithFlywayDataSourceConfiguration;
import io.github.zacharysabourin.donezo_api.dtos.LoginRequest;
import io.github.zacharysabourin.donezo_api.dtos.SignupRequest;
import io.github.zacharysabourin.donezo_api.models.UserProfile;
import io.github.zacharysabourin.donezo_api.utils.JwtUtils;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Import(EmbeddedPostgresWithFlywayDataSourceConfiguration.class)
@Sql(scripts = "/test-seed.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class AuthControllerTest {

    private static final String BASE_URL = "/auth/";
    private static final String VALID_USERNAME = "testmctest";
    private static final String VALID_PASSWORD = "asdfasdf12345678";
    private static final UUID VALID_USER_ID = UUID.fromString("26248245-7afd-42b5-a65b-3e21ea693ce2");

    @Autowired
    private RestTestClient client;

    @Autowired
    private JwtUtils jwtUtil;

    @Value("${donezo.jwt.cookie}")
    private String jwtCookieName;

    private String validJwtToken;
    private String csrfTokenValue;

    @BeforeEach
    void setUp() {
        validJwtToken = jwtUtil.generateTokenFromUsername(VALID_USERNAME);
        csrfTokenValue = "test-csrf-token";
    }

    // =========================================================================
    // GET /auth/profile - Protected Endpoint Tests
    // =========================================================================

    @Test
    void getUser_success() {
        client.get().uri(BASE_URL + "profile")
                .cookie(jwtCookieName, validJwtToken)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserProfile.class)
                .consumeWith(response -> {
                    UserProfile profile = response.getResponseBody();
                    assertNotNull(profile);
                    assertThat(profile.id()).isEqualTo(VALID_USER_ID);
                    assertThat(profile.username()).isEqualTo(VALID_USERNAME);
                });
    }

    @Test
    void getUser_failure_unauthorized_missingToken() {
        client.get().uri(BASE_URL + "profile")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void getUser_failure_unauthorized_invalidToken() {
        client.get().uri(BASE_URL + "profile")
                .cookie(jwtCookieName, "invalid.jwt.token")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // =========================================================================
    // POST /auth/login - Authentication & Cookie Generation Tests
    // =========================================================================

    @Test
    void authenticateUser_success() {
        LoginRequest loginRequest = new LoginRequest(VALID_USERNAME, VALID_PASSWORD);

        client.post().uri(BASE_URL + "login")
                .cookie("XSRF-TOKEN", csrfTokenValue)
                .header("X-XSRF-TOKEN", csrfTokenValue)
                .accept(MediaType.APPLICATION_JSON)
                .body(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueMatches(HttpHeaders.SET_COOKIE, jwtCookieName + "=.*")
                .expectBody(UserProfile.class)
                .consumeWith(response -> {
                    UserProfile profile = response.getResponseBody();
                    assertNotNull(profile);
                    assertThat(profile.username()).isEqualTo(VALID_USERNAME);
                });
    }

    @Test
    void authenticateUser_failure_badCredentials() {
        LoginRequest loginRequest = new LoginRequest(VALID_USERNAME, "wrongPassword");

        client.post().uri(BASE_URL + "login")
                .cookie("XSRF-TOKEN", csrfTokenValue)
                .header("X-XSRF-TOKEN", csrfTokenValue)
                .accept(MediaType.APPLICATION_JSON)
                .body(loginRequest)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void authenticateUser_failure_invalidPayload() {
        LoginRequest loginRequest = new LoginRequest("", "");

        client.post().uri(BASE_URL + "login")
                .cookie("XSRF-TOKEN", csrfTokenValue)
                .header("X-XSRF-TOKEN", csrfTokenValue)
                .accept(MediaType.APPLICATION_JSON)
                .body(loginRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    // =========================================================================
    // POST /auth/signup - Registration Tests
    // =========================================================================

    @Test
    void registerUser_success() {
        SignupRequest signupRequest = new SignupRequest("newuser", "securePass123");

        client.post().uri(BASE_URL + "signup")
                .cookie("XSRF-TOKEN", csrfTokenValue)
                .header("X-XSRF-TOKEN", csrfTokenValue)
                .accept(MediaType.APPLICATION_JSON)
                .body(signupRequest)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void registerUser_failure_duplicateUsername() {
        SignupRequest signupRequest = new SignupRequest(VALID_USERNAME, "password123");

        client.post().uri(BASE_URL + "signup")
                .cookie("XSRF-TOKEN", csrfTokenValue)
                .header("X-XSRF-TOKEN", csrfTokenValue)
                .accept(MediaType.APPLICATION_JSON)
                .body(signupRequest)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void registerUser_failure_invalidPayload() {
        SignupRequest signupRequest = new SignupRequest("", "");

        client.post().uri(BASE_URL + "signup")
                .cookie("XSRF-TOKEN", csrfTokenValue)
                .header("X-XSRF-TOKEN", csrfTokenValue)
                .accept(MediaType.APPLICATION_JSON)
                .body(signupRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    // =========================================================================
    // POST /auth/logout - Session Termination Tests
    // =========================================================================

    @Test
    void logoutUser_success() {
        client.post().uri(BASE_URL + "logout")
                .cookie(jwtCookieName, validJwtToken)
                .cookie("XSRF-TOKEN", csrfTokenValue)
                .header("X-XSRF-TOKEN", csrfTokenValue)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueMatches(HttpHeaders.SET_COOKIE, jwtCookieName + "=;.*");
    }
}
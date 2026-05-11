package com.documat.server.config;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    // Base64 of "testSecretKeyForJWTTokenGenerationInTests123456789" (50 chars → 400 bits)
    private static final String TEST_SECRET =
            "dGVzdFNlY3JldEtleUZvckpXVFRva2VuR2VuZXJhdGlvbkluVGVzdHMxMjM0NTY3ODk=";

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 3_600_000); // 1 hour
    }

    private Authentication buildAuth(String username) {
        return new UsernamePasswordAuthenticationToken(
                org.springframework.security.core.userdetails.User
                        .withUsername(username)
                        .password("irrelevant")
                        .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                        .build(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void generateJwtToken_returnsNonNullToken() {
        String token = jwtUtils.generateJwtToken(buildAuth("alice"));
        assertThat(token).isNotBlank();
    }

    @Test
    void getUserNameFromJwtToken_returnsCorrectUsername() {
        String token = jwtUtils.generateJwtToken(buildAuth("alice"));
        assertThat(jwtUtils.getUserNameFromJwtToken(token)).isEqualTo("alice");
    }

    @Test
    void validateJwtToken_validToken_returnsTrue() {
        String token = jwtUtils.generateJwtToken(buildAuth("alice"));
        assertThat(jwtUtils.validateJwtToken(token)).isTrue();
    }

    @Test
    void validateJwtToken_malformedToken_returnsFalse() {
        assertThat(jwtUtils.validateJwtToken("this.is.not.a.valid.jwt")).isFalse();
    }

    @Test
    void validateJwtToken_emptyString_returnsFalse() {
        assertThat(jwtUtils.validateJwtToken("")).isFalse();
    }

    @Test
    void validateJwtToken_expiredToken_returnsFalse() {
        // Override expiration to -1 ms so the token is already expired
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", -1);
        String expiredToken = jwtUtils.generateJwtToken(buildAuth("alice"));
        assertThat(jwtUtils.validateJwtToken(expiredToken)).isFalse();
    }

    @Test
    void getUserNameFromJwtToken_roundTrip_multipleUsers() {
        for (String name : List.of("alice", "bob", "charlie")) {
            String token = jwtUtils.generateJwtToken(buildAuth(name));
            assertThat(jwtUtils.getUserNameFromJwtToken(token)).isEqualTo(name);
        }
    }
}

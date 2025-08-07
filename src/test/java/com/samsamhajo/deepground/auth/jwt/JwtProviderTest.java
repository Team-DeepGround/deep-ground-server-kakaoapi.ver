package com.samsamhajo.deepground.auth.jwt;

import com.samsamhajo.deepground.auth.exception.AuthErrorCode;
import com.samsamhajo.deepground.auth.exception.AuthException;
import com.samsamhajo.deepground.member.entity.Role;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class JwtProviderTest {

    private JwtProvider jwtProvider;
    private String secret;
    private long accessTokenValidityInSeconds = 60 * 10; // 10분
    private long refreshTokenValidityInSeconds = 60 * 60 * 24 * 7; // 7일

    @BeforeEach
    void setUp() {
        // 256비트(32바이트) 키 생성 및 Base64 인코딩
        SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        secret = Base64.getEncoder().encodeToString(key.getEncoded());

        jwtProvider = new JwtProvider(
                secret,
                accessTokenValidityInSeconds,
                refreshTokenValidityInSeconds
        );
    }

    @Test
    void accessToken_발급_및_검증_성공() {
        // given
        Long memberId = 123L;
        String role = Role.ROLE_USER.name();

        // when
        String accessToken = jwtProvider.createAccessToken(memberId, role);

        // then
        assertNotNull(accessToken);
        assertTrue(jwtProvider.validateToken(accessToken));
        assertThat(jwtProvider.getMemberId(accessToken)).isEqualTo(memberId);
    }

    @Test
    void refreshToken_발급_및_검증_성공() {
        // given
        Long memberId = 456L;
        String role = Role.ROLE_USER.name();

        // when
        String refreshToken = jwtProvider.createRefreshToken(memberId, role);

        // then
        assertNotNull(refreshToken);
        assertTrue(jwtProvider.validateToken(refreshToken));
        assertThat(jwtProvider.getMemberId(refreshToken)).isEqualTo(memberId);
    }

    @Test
    void 잘못된_토큰_검증_실패() {
        // given
        String invalidToken = "invalid.token.value";

        // when
        boolean isValid = jwtProvider.validateToken(invalidToken);

        // then
        assertFalse(isValid);
        assertThrows(AuthException.class, () -> jwtProvider.getMemberId(invalidToken));
    }

    @Test
    void 만료된_토큰_검증_실패() throws InterruptedException {
        // given
        // 만료 시간이 1초인 토큰 발급
        JwtProvider shortLivedProvider = new JwtProvider(secret, 1, 1);
        String token = shortLivedProvider.createAccessToken(789L, Role.ROLE_GUEST.name());

        // 2초 대기하여 토큰 만료 유도
        Thread.sleep(2000);

        // when & then
        assertFalse(shortLivedProvider.validateToken(token));
        AuthException exception = assertThrows(AuthException.class, () -> shortLivedProvider.getMemberId(token));
        assertEquals(AuthErrorCode.EXPIRED_TOKEN, exception.getErrorCode());
    }
}

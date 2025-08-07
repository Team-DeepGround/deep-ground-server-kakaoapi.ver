package com.samsamhajo.deepground.auth.service;

import com.samsamhajo.deepground.auth.dto.LoginRequest;
import com.samsamhajo.deepground.auth.dto.LoginResponse;
import com.samsamhajo.deepground.auth.dto.TokenRefreshRequest;
import com.samsamhajo.deepground.auth.dto.TokenRefreshResponse;
import com.samsamhajo.deepground.auth.exception.AuthErrorCode;
import com.samsamhajo.deepground.auth.exception.AuthException;
import com.samsamhajo.deepground.auth.jwt.JwtProvider;
import com.samsamhajo.deepground.auth.repository.RefreshTokenRepository;
import com.samsamhajo.deepground.member.entity.Member;
import com.samsamhajo.deepground.member.entity.Role;
import com.samsamhajo.deepground.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class RefreshAccessTokenTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Member member;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @BeforeEach
    void setup() {
        member = Member.createLocalMember(
                "test@example.com",
                passwordEncoder.encode("password123"),
                "test01"
        );
        member.verify();
        memberRepository.save(member);
    }

    @Test
    void 로그인_성공_시_리프레시_토큰_저장() {
        // given
        LoginRequest request = new LoginRequest(
                member.getEmail(),
                "password123"
        );

        // when
        LoginResponse response = authService.login(request);

        // then
        String savedRefreshToken = refreshTokenRepository.findByMemberId(member.getId());
        assertNotNull(savedRefreshToken);
        assertEquals(response.getRefreshToken(), savedRefreshToken);
    }

    @Test
    void 액세스토큰_재발급_성공() {
        // given
        String refreshToken = jwtProvider.createRefreshToken(member.getId(), member.getRole().name());
        refreshTokenRepository.save(member.getId(), refreshToken, 3600L);
        TokenRefreshRequest request = new TokenRefreshRequest(refreshToken);

        // when
        TokenRefreshResponse response = authService.refreshAccessToken(request);

        // then
        assertNotNull(response.getAccessToken());
        assertTrue(jwtProvider.validateToken(response.getAccessToken()));
        assertEquals(member.getId(), jwtProvider.getMemberId(response.getAccessToken()));
    }

    @Test
    void 리프레시토큰_만료임박_시_모든토큰_재발급() {
        // given
        long shortExpirationSeconds = 3L; // 3초짜리 만료시간
        String oldRefreshToken = jwtProvider.createTestRefreshToken(member.getId() ,shortExpirationSeconds);
        refreshTokenRepository.save(member.getId(), oldRefreshToken, shortExpirationSeconds);

        TokenRefreshRequest request = new TokenRefreshRequest(oldRefreshToken);

        // when
        TokenRefreshResponse response = authService.refreshAccessToken(request);

        // then
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertNotEquals(oldRefreshToken, response.getRefreshToken());

        String newSavedRefreshToken = refreshTokenRepository.findByMemberId(member.getId());
        assertEquals(response.getRefreshToken(), newSavedRefreshToken);
    }


    @Test
    void 리프레시토큰_유효기간_충분_시_액세스토큰만_재발급() {
        // given
        String refreshToken = jwtProvider.createRefreshToken(member.getId(), member.getRole().name());
        refreshTokenRepository.save(member.getId(), refreshToken, 604800L); // 7일
        TokenRefreshRequest request = new TokenRefreshRequest(refreshToken);

        // when
        TokenRefreshResponse response = authService.refreshAccessToken(request);

        // then
        assertNotNull(response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());

        // Redis의 리프레시 토큰이 그대로인지 확인
        String savedRefreshToken = refreshTokenRepository.findByMemberId(member.getId());
        assertEquals(refreshToken, savedRefreshToken);
    }

    @Test
    void 저장되지_않은_리프레시토큰으로_재발급_실패() {
        // given
        String refreshToken = jwtProvider.createRefreshToken(member.getId(), member.getRole().name());
        TokenRefreshRequest request = new TokenRefreshRequest(refreshToken);

        // when & then
        AuthException exception = assertThrows(AuthException.class,
                () -> authService.refreshAccessToken(request));
        assertEquals(AuthErrorCode.INVALID_REFRESH_TOKEN, exception.getErrorCode());
    }

    @Test
    void 유효하지_않은_리프레시토큰으로_재발급_실패() {
        // given
        TokenRefreshRequest request = new TokenRefreshRequest("invalid_token");

        // when & then
        AuthException exception = assertThrows(AuthException.class,
                () -> authService.refreshAccessToken(request));
        assertEquals(AuthErrorCode.INVALID_REFRESH_TOKEN, exception.getErrorCode());
    }

    @Test
    void 다른_리프레시토큰_값으로_재발급_실패() {
        // given
        String savedRefreshToken = jwtProvider.createRefreshToken(member.getId(), member.getRole().name());
        refreshTokenRepository.save(member.getId(), savedRefreshToken, 3600L);

        Member member2 = Member.createLocalMember(
                "test12345@example.com",
                passwordEncoder.encode("password123"),
                "test12345"
        );

        member2.verify();
        memberRepository.save(member2);

        String differentRefreshToken = jwtProvider.createRefreshToken(member2.getId(), member2.getRole().name());
        TokenRefreshRequest request = new TokenRefreshRequest(differentRefreshToken);

        // when & then
        AuthException exception = assertThrows(AuthException.class,
                () -> authService.refreshAccessToken(request));
        assertEquals(AuthErrorCode.INVALID_REFRESH_TOKEN, exception.getErrorCode());
    }
}

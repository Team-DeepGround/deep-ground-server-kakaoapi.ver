package com.samsamhajo.deepground.auth.service;

import com.samsamhajo.deepground.auth.dto.*;
import com.samsamhajo.deepground.auth.exception.AuthErrorCode;
import com.samsamhajo.deepground.auth.exception.AuthException;
import com.samsamhajo.deepground.auth.jwt.JwtProvider;
import com.samsamhajo.deepground.email.dto.EmailRequest;
import com.samsamhajo.deepground.email.repository.EmailVerificationRepository;
import com.samsamhajo.deepground.email.service.EmailService;
import com.samsamhajo.deepground.auth.repository.RefreshTokenRepository;

import com.samsamhajo.deepground.member.entity.Member;
import com.samsamhajo.deepground.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final EmailService emailService;
    private final EmailVerificationRepository emailVerificationRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private Long refreshTokenValidityInSeconds;
    // 리프레시 토큰 재발급 기준 시간
    private static final long REISSUE_REFRESH_TOKEN_TIME = 259200L;  // 3일을 초 단위로

    @Transactional
    public Long register(RegisterRequest request) {

        // 중복 검사
        checkEmailDuplicate(request.getEmail());
        checkNicknameDuplicate(request.getNickname());

        Member member = Member.createLocalMember(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getNickname()
        );

        Member savedMember = memberRepository.save(member);
        return savedMember.getId();
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {

        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_EMAIL));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new AuthException(AuthErrorCode.INVALID_PASSWORD);
        }

        if (member.isBanned()) {
            throw new AuthException(AuthErrorCode.BANNED_MEMBER);
        }

        String accessToken = jwtProvider.createAccessToken(member.getId(), member.getRole().name());
        String refreshToken = jwtProvider.createRefreshToken(member.getId(), member.getRole().name());

        refreshTokenRepository.save(
                member.getId(),
                refreshToken,
                refreshTokenValidityInSeconds
        );

        return new LoginResponse(
                accessToken,
                refreshToken,
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getRole()
        );
    }

    @Transactional
    public PasswordResetEmailResponse sendPasswordResetEmail(PasswordResetRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_EMAIL));

        EmailRequest emailRequest = new EmailRequest(request.getEmail());
        emailService.sendVerificationEmail(emailRequest);

        return PasswordResetEmailResponse.of(request.getEmail());
    }

    @Transactional
    public PasswordResetResponse resetPassword(PasswordResetVerifyRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_EMAIL));

        String storedCode = emailVerificationRepository.getCode(request.getEmail());
        if (storedCode == null) {
            throw new AuthException(AuthErrorCode.VERIFICATION_CODE_EXPIRED);
        }
        if (!storedCode.equals(request.getCode())) {
            throw new AuthException(AuthErrorCode.INVALID_VERIFICATION_CODE);
        }

        member.updatePassword(passwordEncoder.encode(request.getNewPassword()));

        emailVerificationRepository.delete(request.getEmail());

        return PasswordResetResponse.of(member);
    }
    
    @Transactional
    public TokenRefreshResponse refreshAccessToken(TokenRefreshRequest request) {

        if (!jwtProvider.validateToken(request.getRefreshToken())) {
            throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
        Long memberId = jwtProvider.getMemberId(request.getRefreshToken());
        String role = jwtProvider.getRole(request.getRefreshToken());

        String savedRefreshToken = refreshTokenRepository.findByMemberId(memberId);
        if (savedRefreshToken == null || !savedRefreshToken.equals(request.getRefreshToken())) {
            throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        String newAccessToken = jwtProvider.createAccessToken(memberId, role);
        String currentRefreshToken = request.getRefreshToken();

        long remainingTime = jwtProvider.getRemainingTime(currentRefreshToken);
        if (remainingTime <= REISSUE_REFRESH_TOKEN_TIME) {
            String newRefreshToken = jwtProvider.createRefreshToken(memberId, role);
            // 새로운 리프레시 토큰을 redis에 저장
            refreshTokenRepository.save(memberId, newRefreshToken, refreshTokenValidityInSeconds);

            return new TokenRefreshResponse(newAccessToken, newRefreshToken);
        }

        return new TokenRefreshResponse(newAccessToken, currentRefreshToken);
    }

    @Transactional
    public void logout(Long memberId) {
        //Redis에서 Refresh Token 삭제
        refreshTokenRepository.deleteByMemberId(memberId);
    }

    public void checkEmailDuplicate(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new AuthException(AuthErrorCode.DUPLICATE_EMAIL);
        }
    }

    public void checkNicknameDuplicate(String nickname) {
        if (memberRepository.existsByNickname(nickname)) {
            throw new AuthException(AuthErrorCode.DUPLICATE_NICKNAME);
        }
    }
}

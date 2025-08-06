package com.samsamhajo.deepground.auth.oauth;

import com.samsamhajo.deepground.auth.jwt.JwtProvider;
import com.samsamhajo.deepground.auth.repository.RefreshTokenRepository;
import com.samsamhajo.deepground.auth.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.oauth2.authorized-redirect-uri}")
    private String oauth2RedirectUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String accessToken = jwtProvider.createAccessToken(userDetails.getMember().getId(), userDetails.getMember().getRole().name());
        String refreshToken = jwtProvider.createRefreshToken(userDetails.getMember().getId(), userDetails.getMember().getRole().name());

        refreshTokenRepository.save(userDetails.getMember().getId(), refreshToken, 1209600L);

        String nickname = URLEncoder.encode(userDetails.getMember().getNickname(), StandardCharsets.UTF_8);
        String email = URLEncoder.encode(userDetails.getMember().getEmail(), StandardCharsets.UTF_8);

        String targetUrl = UriComponentsBuilder.fromUriString(oauth2RedirectUri)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("email", email)
                .queryParam("nickname", nickname)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

package com.samsamhajo.deepground.auth.jwt;

import com.samsamhajo.deepground.auth.security.CustomUserDetailsService;
import com.samsamhajo.deepground.member.entity.Member;
import com.samsamhajo.deepground.member.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;
    private final MemberRepository memberRepository;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String jwt = resolveToken(request);

        if (StringUtils.hasText(jwt) && jwtProvider.validateToken(jwt)) {
            Long memberId = jwtProvider.getMemberId(jwt);
            String role = jwtProvider.getRole(jwt);

            Member member = memberRepository.findById(memberId).orElse(null);
            if (member != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(member.getEmail());
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        List.of(new SimpleGrantedAuthority(role))
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false; // 비동기 요청(SSE)에 필터 적용
    }
}

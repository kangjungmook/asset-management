package com.company.ams.security;

import com.company.ams.entity.User;
import com.company.ams.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserMapper userMapper;
    private final PrincipalFactory principalFactory;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtProvider.isValid(token)) {
                Claims claims = jwtProvider.parseClaims(token);
                if (!jwtProvider.isRefreshToken(claims)) {
                    // 토큰은 신원 확인(서명·만료)에만 쓰고, 활성 상태·역할·권한은 매 요청마다
                    // DB에서 새로 읽어 반영한다. 그래야 비활성화·권한 회수가 즉시 적용된다.
                    Integer userId = Integer.valueOf(claims.getSubject());
                    User user = userMapper.findById(userId);
                    if (user != null && Boolean.TRUE.equals(user.getIsActive())) {
                        AuthPrincipal principal = principalFactory.from(user);

                        List<GrantedAuthority> authorities = new ArrayList<>();
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + (principal.isAdmin() ? "ADMIN" : "USER")));
                        if (principal.getRoles() != null) {
                            principal.getRoles().forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r)));
                        }

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(principal, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}

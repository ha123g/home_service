package com.example.home_service_backend.security;

import com.example.home_service_backend.common.constants.ApiConstants;
import com.example.home_service_backend.common.constants.AuthConstants;
import com.example.home_service_backend.common.result.ResultFactory;
import com.example.home_service_backend.dto.request.auth.LoginRequest;
import com.example.home_service_backend.vo.auth.LoginResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import java.io.IOException;
import java.util.List;

/** JSON 登录过滤器；登录请求不会进入 UsernamePasswordAuthenticationFilter。 */
public final class LoginFilter extends AbstractAuthenticationProcessingFilter {
    private final ObjectMapper objectMapper;
    private final SecurityContextRepository securityContextRepository;


    public LoginFilter(AuthenticationManager authenticationManager,
                       ObjectMapper objectMapper,
                       SecurityContextRepository securityContextRepository) {
        super(
                PathPatternRequestMatcher.withDefaults()
                        .matcher(HttpMethod.POST, ApiConstants.AUTH_PREFIX + "/login"),
                authenticationManager
        );
        this.objectMapper = objectMapper;
        this.securityContextRepository = securityContextRepository;
    }
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (request.getContentType() == null || !request.getContentType().startsWith(MediaType.APPLICATION_JSON_VALUE)) {
            throw new AuthenticationServiceException("登录请求必须使用 application/json");
        }
        LoginRequest credentials;
        try {
            credentials = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
        } catch (Exception exception) {
            throw new AuthenticationServiceException("登录参数格式错误", exception);
        }
        if (credentials.username() == null || credentials.username().isBlank()
                || credentials.password() == null || credentials.password().isBlank()) {
            throw new AuthenticationServiceException("用户名或密码错误");
        }
        return getAuthenticationManager().authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(credentials.username(), credentials.password()));
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            jakarta.servlet.FilterChain chain, Authentication authentication)
            throws IOException, ServletException {
        if (request.getSession(false) != null) request.changeSessionId();
        request.getSession(true).setMaxInactiveInterval(AuthConstants.SESSION_TIMEOUT_SECONDS);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        LoginUser user = (LoginUser) authentication.getPrincipal();
        List<String> authorities = authentication.getAuthorities().stream()
                .map(granted -> granted.getAuthority()).toList();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), ResultFactory.buildSuccessData(
                new LoginResponse(user.getId(), user.getUsername(), authorities)));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), ResultFactory.buildError(
                ResultFactory.UNAUTHORIZED_CODE, "用户名或密码错误"));
    }
}

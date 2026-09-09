package com.example.home_service_backend.config;

import com.example.home_service_backend.common.constants.ApiConstants;
import com.example.home_service_backend.security.RestAccessDeniedHandler;
import com.example.home_service_backend.security.RestAuthenticationEntryPoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.home_service_backend.security.LoginFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            org.springframework.security.authentication.AuthenticationManager authenticationManager,
                                            ObjectMapper objectMapper,
                                            SecurityContextRepository contextRepository,
                                            RestAuthenticationEntryPoint authenticationEntryPoint,
                                            RestAccessDeniedHandler accessDeniedHandler) throws Exception {
        LoginFilter loginFilter = new LoginFilter(authenticationManager, objectMapper, contextRepository);
        http
            .securityContext(context -> context.securityContextRepository(contextRepository))
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .sessionFixation(fixation -> fixation.changeSessionId()))
            .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(ApiConstants.AUTH_PREFIX + "/login", ApiConstants.AUTH_PREFIX + "/register",
                            ApiConstants.AUTH_PREFIX + "/csrf", ApiConstants.AUTH_PREFIX + "/logout",
                            "/actuator/health", "/error").permitAll()
                    .anyRequest().authenticated())
            .logout(logout -> logout.disable())
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable());
        http.addFilterBefore(loginFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

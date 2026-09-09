package com.example.home_service_backend.service;

import com.example.home_service_backend.common.enums.AuthRoleEnum;
import com.example.home_service_backend.common.enums.AuthUserStateEnum;
import com.example.home_service_backend.common.exception.BusinessException;
import com.example.home_service_backend.dto.request.auth.RegisterRequest;
import com.example.home_service_backend.vo.auth.RegisterResponse;
import com.example.home_service_backend.entity.User;
import com.example.home_service_backend.entity.UserProfile;
import com.example.home_service_backend.entity.UserRole;
import com.example.home_service_backend.repository.UserProfileRepository;
import com.example.home_service_backend.repository.UserRepository;
import com.example.home_service_backend.repository.UserRoleRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final String sessionCookieName;

    public AuthService(UserRepository userRepository,
                       UserRoleRepository userRoleRepository,
                       UserProfileRepository userProfileRepository,
                       PasswordEncoder passwordEncoder,
                       @Value("${server.servlet.session.cookie.name:SESSION}") String sessionCookieName) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionCookieName = sessionCookieName;
    }

    /**
     * 注册普通用户：写入用户主表、默认角色与扩展资料。
     */
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException("409", "用户名已存在");
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setStatus(AuthUserStateEnum.USER_STATE_NORMAL.getValue());
        user.setCreatedTime(now);
        user.setUpdatedTime(now);

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException("409", "用户名已存在");
        }

        UserRole userRole = new UserRole();
        userRole.setId(user.getId());
        userRole.setUserId(user.getId());
        userRole.setRoleId((long) AuthRoleEnum.USERNAME_NORMAL_USER.getRoleCode());
        userRoleRepository.save(userRole);

        UserProfile profile = new UserProfile();
        profile.setId(user.getId());
        profile.setUserId(user.getId());
        String nickname = request.nickname();
        profile.setNickname(nickname == null || nickname.isBlank() ? request.username() : nickname.trim());
        profile.setCreatedAt(now);
        profile.setUpdatedAt(now);
        userProfileRepository.save(profile);

        return new RegisterResponse(user.getId(), user.getUsername());
    }

    /**
     * 退出登录：清理 SecurityContext、销毁 Redis Session 并清除 Cookie。
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.setInvalidateHttpSession(true);
        logoutHandler.setClearAuthentication(true);
        logoutHandler.logout(request, response, authentication);

        jakarta.servlet.http.Cookie sessionCookie = new jakarta.servlet.http.Cookie(sessionCookieName, null);
        sessionCookie.setPath("/");
        sessionCookie.setMaxAge(0);
        sessionCookie.setHttpOnly(true);
        response.addCookie(sessionCookie);
    }
}

package com.example.home_service_backend.service;

import com.example.home_service_backend.common.enums.AuthRoleEnum;
import com.example.home_service_backend.common.enums.AuthUserStateEnum;
import com.example.home_service_backend.common.enums.FileUploadPurpose;
import com.example.home_service_backend.common.exception.BusinessException;
import com.example.home_service_backend.common.result.ResultFactory;
import com.example.home_service_backend.dto.request.user.AdminCreateUserRequest;
import com.example.home_service_backend.dto.request.user.ChangePasswordRequest;
import com.example.home_service_backend.dto.request.user.UpdateUserProfileRequest;
import com.example.home_service_backend.vo.user.UserPageView;
import com.example.home_service_backend.vo.user.UserView;
import com.example.home_service_backend.entity.User;
import com.example.home_service_backend.entity.UserProfile;
import com.example.home_service_backend.entity.UserRole;
import com.example.home_service_backend.repository.UserProfileRepository;
import com.example.home_service_backend.repository.UserRepository;
import com.example.home_service_backend.repository.UserRoleRepository;
import com.example.home_service_backend.repository.UserSpecifications;
import com.example.home_service_backend.security.LoginUser;
import com.example.home_service_backend.common.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectProvider<FindByIndexNameSessionRepository<? extends Session>> sessionRepository;
    private final CosObjectStorageService cosObjectStorageService;

    public UserService(UserRepository userRepository,
                       UserProfileRepository profileRepository,
                       UserRoleRepository userRoleRepository,
                       PasswordEncoder passwordEncoder,
                       ObjectProvider<FindByIndexNameSessionRepository<? extends Session>> sessionRepository,
                       CosObjectStorageService cosObjectStorageService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionRepository = sessionRepository;
        this.cosObjectStorageService = cosObjectStorageService;
    }

    @Transactional(readOnly = true)
    public UserPageView listUsers(int page, int size, String keyword, String status) {
        Pageable pageable = PageRequest.of(page, size);
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        String normalizedStatus = status == null || status.isBlank() ? null : status.trim();
        if (normalizedStatus != null && !isKnownStatus(normalizedStatus)) {
            throw new BusinessException("400", "用户状态不合法");
        }
        Page<User> pageData = userRepository.findAll(
                UserSpecifications.byKeywordAndStatus(normalizedKeyword, normalizedStatus), pageable);
        Map<Long, UserProfile> profiles = profilesByUserId(pageData.getContent().stream().map(User::getId).toList());
        List<UserView> items = pageData.getContent().stream()
                .map(user -> toView(user, profiles.get(user.getId())))
                .toList();
        return new UserPageView(items, pageData.getTotalElements(), page, size);
    }

    @Transactional(readOnly = true)
    public UserView getUser(Long userId) {
        User user = findUser(userId);
        return toView(user, profileRepository.findByUserId(userId).orElse(null));
    }

    @Transactional
    public UserView createByAdmin(AdminCreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException("409", "用户名已存在");
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        User user = new User();
        user.setUsername(request.username().trim());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setStatus(AuthUserStateEnum.USER_STATE_NORMAL.getValue());
        user.setCreatedTime(now);
        user.setUpdatedTime(now);
        try {
            user = userRepository.saveAndFlush(user);
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
        profile.setNickname(request.nickname() == null || request.nickname().isBlank()
                ? user.getUsername() : request.nickname().trim());
        profile.setCreatedAt(now);
        profile.setUpdatedAt(now);
        profileRepository.save(profile);
        return toView(user, profile);
    }

    @Transactional
    public void delete(Long userId) {
        User user = findUser(userId);
        rejectSelfOperation(userId, "不能删除当前登录账号");
        user.setStatus(AuthUserStateEnum.USER_STATE_LOGOUT.getValue());
        user.setUpdatedTime(LocalDateTime.now(ZoneOffset.UTC));
        userRepository.save(user);
        invalidateSessions(user.getUsername());
    }

    @Transactional
    public void lock(Long userId) {
        User user = findUser(userId);
        rejectSelfOperation(userId, "不能锁定当前登录账号");
        user.setStatus(AuthUserStateEnum.USER_STATE_LOCKED.getValue());
        user.setLockTime(LocalDateTime.now(ZoneOffset.UTC));
        user.setUpdatedTime(LocalDateTime.now(ZoneOffset.UTC));
        userRepository.save(user);
        invalidateSessions(user.getUsername());
    }

    @Transactional
    public void unlock(Long userId) {
        User user = findUser(userId);
        user.setStatus(AuthUserStateEnum.USER_STATE_NORMAL.getValue());
        user.setLockTime(null);
        user.setUpdatedTime(LocalDateTime.now(ZoneOffset.UTC));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserView currentUser() {
        LoginUser loginUser = SecurityUtils.requireLoginUser();
        User user = findUser(loginUser.getId());
        return toView(user, profileRepository.findByUserId(user.getId()).orElse(null));
    }

    @Transactional
    public UserView updateCurrentProfile(UpdateUserProfileRequest request) {
        LoginUser loginUser = SecurityUtils.requireLoginUser();
        User user = findUser(loginUser.getId());
        UserProfile profile = profileRepository.findByUserId(user.getId()).orElseGet(() -> {
            UserProfile created = new UserProfile();
            created.setId(user.getId());
            created.setUserId(user.getId());
            created.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
            return created;
        });
        if (request.nickname() != null) profile.setNickname(request.nickname().trim());
        if (request.avatarUrl() != null) {
            if (request.avatarUrl().isBlank()) {
                profile.setAvatarUrl(null);
            } else {
                CosObjectKeyPolicy.ParsedKey key = cosObjectStorageService.requireOwnedKey(
                        request.avatarUrl(), user.getId(), java.util.Set.of(FileUploadPurpose.AVATAR));
                profile.setAvatarUrl(key.objectKey());
            }
        }
        if (request.gender() != null) profile.setGender(request.gender().trim());
        profile.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
        profileRepository.save(profile);
        return toView(user, profile);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, HttpServletRequest httpRequest) {
        LoginUser loginUser = SecurityUtils.requireLoginUser();
        User user = findUser(loginUser.getId());
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BusinessException("400", "两次输入的新密码不一致");
        }
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            // 原密码校验失败不代表 Session 失效。使用参数错误码，避免前端按 401
            // 撤销当前会话并跳转登录页；用户仍可留在修改密码弹窗中重试。
            throw new BusinessException("400", "原密码错误");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BusinessException("400", "新密码不能与原密码相同");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setUpdatedTime(LocalDateTime.now(ZoneOffset.UTC));
        userRepository.save(user);
        if (httpRequest.getSession(false) != null) httpRequest.changeSessionId();
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("404", "用户不存在"));
    }

    private boolean isKnownStatus(String status) {
        for (AuthUserStateEnum state : AuthUserStateEnum.values()) {
            if (state.getValue().equals(status)) return true;
        }
        return false;
    }

    private void rejectSelfOperation(Long userId, String message) {
        if (userId.equals(SecurityUtils.requireLoginUser().getId())) {
            throw new BusinessException("409", message);
        }
    }

    private void invalidateSessions(String username) {
        FindByIndexNameSessionRepository<? extends Session> repository = sessionRepository.getIfAvailable();
        if (repository == null) return;
        repository.findByPrincipalName(username).keySet().forEach(repository::deleteById);
    }

    private Map<Long, UserProfile> profilesByUserId(Collection<Long> ids) {
        if (ids.isEmpty()) return Map.of();
        Map<Long, UserProfile> result = new LinkedHashMap<>();
        profileRepository.findByUserIdIn(ids).forEach(profile -> result.put(profile.getUserId(), profile));
        return result;
    }

    private UserView toView(User user, UserProfile profile) {
        String avatarKey = profile == null ? null : profile.getAvatarUrl();
        String avatarPreview = cosObjectStorageService.previewPath(avatarKey);
        return new UserView(user.getId(), user.getUsername(), user.getStatus(),
                profile == null ? null : profile.getNickname(),
                avatarPreview,
                profile == null ? null : profile.getGender(), user.getLockTime(),
                user.getCreatedTime(), user.getUpdatedTime());
    }
}

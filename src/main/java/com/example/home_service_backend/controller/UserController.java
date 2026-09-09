package com.example.home_service_backend.controller;

import com.example.home_service_backend.common.constants.ApiConstants;
import com.example.home_service_backend.common.result.ResultBase;
import com.example.home_service_backend.common.result.ResultData;
import com.example.home_service_backend.common.result.ResultFactory;
import com.example.home_service_backend.dto.request.user.AdminCreateUserRequest;
import com.example.home_service_backend.dto.request.user.ChangePasswordRequest;
import com.example.home_service_backend.dto.request.user.UpdateUserProfileRequest;
import com.example.home_service_backend.service.UserService;
import com.example.home_service_backend.vo.user.UserPageView;
import com.example.home_service_backend.vo.user.UserView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.USERS_PREFIX)
@Validated
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 列出所有用户
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('username_super_admin','username_sec_admin','username_sys_admin','username_aud_admin')")
    public ResponseEntity<ResultData<UserPageView>> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ResultFactory.buildSuccessData(
                userService.listUsers(page, size, keyword, status)));
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('username_super_admin','username_sec_admin','username_sys_admin','username_aud_admin')")
    public ResponseEntity<ResultData<UserView>> detail(@PathVariable Long userId) {
        return ResponseEntity.ok(ResultFactory.buildSuccessData(userService.getUser(userId)));
    }

    /**
     * 创建用户
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('username_super_admin','username_sys_admin')")
    public ResponseEntity<ResultData<UserView>> create(@Valid @RequestBody AdminCreateUserRequest request) {
        return ResponseEntity.status(201).body(ResultFactory.buildSuccessData(
                userService.createByAdmin(request)));
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyRole('username_super_admin','username_sys_admin')")
    public ResponseEntity<ResultBase> delete(@PathVariable Long userId) {
        userService.delete(userId);
        return ResponseEntity.ok(ResultFactory.buildSuccessBase());
    }

    /**
     * 锁定用户
     */
    @PutMapping("/{userId}/lock")
    @PreAuthorize("hasAnyRole('username_super_admin','username_sec_admin','username_sys_admin')")
    public ResponseEntity<ResultBase> lock(@PathVariable Long userId) {
        userService.lock(userId);
        return ResponseEntity.ok(ResultFactory.buildSuccessBase());
    }

    /**
     * 解锁用户
     */
    @PutMapping("/{userId}/unlock")
    @PreAuthorize("hasAnyRole('username_super_admin','username_sec_admin','username_sys_admin')")
    public ResponseEntity<ResultBase> unlock(@PathVariable Long userId) {
        userService.unlock(userId);
        return ResponseEntity.ok(ResultFactory.buildSuccessBase());
    }

    /**
     * 获取当前用户详情
     */
    @GetMapping("/me")
    public ResponseEntity<ResultData<UserView>> me() {
        return ResponseEntity.ok(ResultFactory.buildSuccessData(userService.currentUser()));
    }

    /**
     * 更新当前用户资料
     */
    @PutMapping("/me/profile")
    public ResponseEntity<ResultData<UserView>> updateProfile(
            @Valid @RequestBody UpdateUserProfileRequest request) {
        return ResponseEntity.ok(ResultFactory.buildSuccessData(
                userService.updateCurrentProfile(request)));
    }

    /**
     * 修改当前用户密码
     */
    @PutMapping("/me/password")
    public ResponseEntity<ResultBase> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest) {
        userService.changePassword(request, httpRequest);
        return ResponseEntity.ok(ResultFactory.buildSuccessBase());
    }
}

package com.example.home_service_backend.controller;

import com.example.home_service_backend.common.constants.ApiConstants;
import com.example.home_service_backend.common.result.ResultBase;
import com.example.home_service_backend.common.result.ResultData;
import com.example.home_service_backend.common.result.ResultFactory;
import com.example.home_service_backend.common.utils.SecurityUtils;
import com.example.home_service_backend.dto.request.auth.RegisterRequest;
import com.example.home_service_backend.security.LoginUser;
import com.example.home_service_backend.service.AuthService;
import com.example.home_service_backend.vo.auth.CsrfTokenView;
import com.example.home_service_backend.vo.auth.LoginResponse;
import com.example.home_service_backend.vo.auth.RegisterResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.AUTH_PREFIX)
@Validated
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // @Valid表示启动校验校验规则在对应的对象中定义
    /** 注册 */
    @PostMapping("/register")
    public ResponseEntity<ResultData<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.ok(ResultFactory.buildSuccessData(response));
    }

    /** 登出并销毁 Redis Session；登录本身由 LoginFilter 处理。 */
    @PostMapping("/logout")
    public ResponseEntity<ResultBase> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.ok(ResultFactory.buildSuccessBase());
    }

    /** 返回当前 Session 中的认证主体，不重新暴露密码或数据库实体。 */
    @GetMapping("/me")
    public ResponseEntity<ResultData<LoginResponse>> currentUser() {
        LoginUser user = SecurityUtils.requireLoginUser();
        List<String> authorities = user.getAuthorities().stream()
                .map(granted -> granted.getAuthority())
                .toList();
        return ResponseEntity.ok(ResultFactory.buildSuccessData(
                new LoginResponse(user.getId(), user.getUsername(), authorities)));
    }

    /** 获取 CSRF Token；浏览器随后在写请求中回传 X-XSRF-TOKEN。 */
    @GetMapping("/csrf")
    public ResponseEntity<ResultData<CsrfTokenView>> csrf(CsrfToken token) {
        return ResponseEntity.ok(ResultFactory.buildSuccessData(
                new CsrfTokenView(token.getHeaderName(), token.getParameterName(), token.getToken())));
    }
}

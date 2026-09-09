package com.example.home_service_backend.security;

import com.example.home_service_backend.common.enums.AuthUserStateEnum;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/** 已通过数据库认证的用户快照，序列化后由 Spring Session 保存到 Redis。 */
public final class LoginUser implements UserDetails {
    private final Long id;
    private final String username;
    // 不把密码哈希写入 Redis Session；认证完成后后续请求只依赖已签名的安全上下文。
    private final transient String password;
    private final String status;
    private final List<GrantedAuthority> authorities;

    public LoginUser(Long id, String username, String password, String status,
                     Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.status = status;
        this.authorities = List.copyOf(authorities);
    }

    public Long getId() { return id; }
    public String getStatus() { return status; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() {
        return !AuthUserStateEnum.USER_STATE_LOCKED.getValue().equalsIgnoreCase(status);
    }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() {
        return AuthUserStateEnum.USER_STATE_NORMAL.getValue().equalsIgnoreCase(status);
    }
}

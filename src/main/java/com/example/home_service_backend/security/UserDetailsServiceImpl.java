package com.example.home_service_backend.security;

import com.example.home_service_backend.common.constants.AuthConstants;
import com.example.home_service_backend.common.enums.AuthRoleEnum;
import com.example.home_service_backend.entity.Permission;
import com.example.home_service_backend.entity.RolePermission;
import com.example.home_service_backend.entity.User;
import com.example.home_service_backend.entity.UserRole;
import com.example.home_service_backend.repository.PermissionRepository;
import com.example.home_service_backend.repository.RolePermissionRepository;
import com.example.home_service_backend.repository.UserRepository;
import com.example.home_service_backend.repository.UserRoleRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;

    public UserDetailsServiceImpl(UserRepository userRepository,
                                  UserRoleRepository userRoleRepository,
                                  RolePermissionRepository rolePermissionRepository,
                                  PermissionRepository permissionRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public LoginUser loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户名或密码错误"));

        Set<SimpleGrantedAuthority> authorities = new LinkedHashSet<>();
        List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());
        Set<Long> roleIds = userRoles.stream().map(UserRole::getRoleId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        for (Long roleId : roleIds) {
            AuthRoleEnum role = roleByCode(roleId.intValue());
            if (role != null) {
                authorities.add(new SimpleGrantedAuthority(AuthConstants.ROLE_PREFIX + role.getValue()));
            }
        }
        if (!roleIds.isEmpty()) {
            Set<Long> permissionIds = rolePermissionRepository.findByRoleIdIn(roleIds).stream()
                    .map(RolePermission::getPermissionId).filter(Objects::nonNull).collect(Collectors.toSet());
            if (!permissionIds.isEmpty()) {
                permissionRepository.findByIdIn(permissionIds).stream().map(Permission::getCode)
                        .filter(code -> code != null && !code.isBlank())
                        .map(SimpleGrantedAuthority::new)
                        .forEach(authorities::add);
            }
        }
        return new LoginUser(user.getId(), user.getUsername(), user.getPassword(), user.getStatus(), authorities);
    }

    private AuthRoleEnum roleByCode(int code) {
        for (AuthRoleEnum role : AuthRoleEnum.values()) {
            if (role.getRoleCode() == code) return role;
        }
        return null;
    }

}

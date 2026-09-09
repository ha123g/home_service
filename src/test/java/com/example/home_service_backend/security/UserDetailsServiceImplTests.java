package com.example.home_service_backend.security;

import com.example.home_service_backend.common.enums.AuthRoleEnum;
import com.example.home_service_backend.entity.Permission;
import com.example.home_service_backend.entity.RolePermission;
import com.example.home_service_backend.entity.User;
import com.example.home_service_backend.entity.UserRole;
import com.example.home_service_backend.repository.PermissionRepository;
import com.example.home_service_backend.repository.RolePermissionRepository;
import com.example.home_service_backend.repository.UserRepository;
import com.example.home_service_backend.repository.UserRoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTests {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private RolePermissionRepository rolePermissionRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadsRolesAndPermissionsOnlyThroughRepositories() {
        User user = new User();
        user.setId(7L);
        user.setUsername("test-user");
        user.setPassword("encoded-password");
        user.setStatus("normal");

        UserRole userRole = new UserRole();
        userRole.setRoleId((long) AuthRoleEnum.USERNAME_NORMAL_USER.getRoleCode());

        RolePermission rolePermission = new RolePermission();
        rolePermission.setPermissionId(11L);

        Permission permission = new Permission();
        permission.setId(11L);
        permission.setCode("user:profile:read");

        when(userRepository.findByUsername("test-user")).thenReturn(Optional.of(user));
        when(userRoleRepository.findByUserId(7L)).thenReturn(List.of(userRole));
        when(rolePermissionRepository.findByRoleIdIn(anyCollection())).thenReturn(List.of(rolePermission));
        when(permissionRepository.findByIdIn(anyCollection())).thenReturn(List.of(permission));

        LoginUser loginUser = userDetailsService.loadUserByUsername("test-user");

        assertThat(loginUser.getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .containsExactlyInAnyOrder("ROLE_username_normal_user", "user:profile:read");
    }
}

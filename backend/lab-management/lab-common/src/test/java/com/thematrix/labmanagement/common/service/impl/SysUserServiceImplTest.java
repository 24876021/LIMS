package com.thematrix.labmanagement.common.service.impl;

import com.thematrix.labmanagement.common.entity.SysUser;
import com.thematrix.labmanagement.common.handler.password.PasswordEncoder;
import com.thematrix.labmanagement.common.service.*;
import com.thematrix.labmanagement.common.utils.RedisUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * SysUserServiceImpl 单元测试
 * 聚焦测试：register() 账号唯一性校验、getUserRoleAndAuthority() 空值检查
 */
@ExtendWith(MockitoExtension.class)
class SysUserServiceImplTest {

    @Mock
    private UserRoleService userRoleService;
    @Mock
    private RoleAuthorityService roleAuthorityService;
    @Mock
    private AuthorityService authorityService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private NotificationService notificationService;
    @Mock
    private RedisUtil redisUtil;
    @Mock
    private RoleService roleService;
    @Mock
    private DepartmentService departmentService;

    @Spy
    @InjectMocks
    private SysUserServiceImpl sysUserService;

    // ==================== register() ====================

    /**
     * 账号已存在时，register 应抛出 RuntimeException
     */
    @Test
    void register_accountExists_throwsException() {
        // 1. 准备：mock getUserByAccount 返回已存在的用户
        SysUser existing = new SysUser();
        existing.setAccount("admin");
        doReturn(existing).when(sysUserService).getUserByAccount("admin");

        SysUser newUser = new SysUser();
        newUser.setAccount("admin");
        newUser.setPassword("encrypted");

        // 2. 执行 + 验证
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> sysUserService.register(newUser));
        assertEquals("当前账号已经存在，请更换账号", ex.getMessage());

        // 3. 确认没有执行保存操作
        verify(sysUserService, never()).save(any(SysUser.class));
    }

    // ==================== getUserRoleAndAuthority() ====================

    /**
     * 用户没有任何角色时，getUserRoleAndAuthority 应抛出 RuntimeException
     */
    @Test
    void getUserRoleAndAuthority_noRole_throwsException() {
        Long userId = 1L;
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setAccount("testuser");
        doReturn(user).when(sysUserService).getById(userId);

        when(userRoleService.getRolesByUserId(userId)).thenReturn(Collections.emptyList());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> sysUserService.getUserRoleAndAuthority(userId));
        assertEquals("用户没有角色", ex.getMessage());
    }

    /**
     * 用户有角色ID但角色名称为空时，getUserRoleAndAuthority 应抛出 RuntimeException
     */
    @Test
    void getUserRoleAndAuthority_emptyRoleNames_throwsException() {
        Long userId = 1L;
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setAccount("testuser");
        doReturn(user).when(sysUserService).getById(userId);

        List<Long> roleIds = Collections.singletonList(1L);
        when(userRoleService.getRolesByUserId(userId)).thenReturn(roleIds);
        when(roleService.getUserRoleName(roleIds)).thenReturn(Collections.emptyList());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> sysUserService.getUserRoleAndAuthority(userId));
        assertEquals("用户角色信息为空", ex.getMessage());
    }
}

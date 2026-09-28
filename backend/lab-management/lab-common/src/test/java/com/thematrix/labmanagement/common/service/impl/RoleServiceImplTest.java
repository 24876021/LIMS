package com.thematrix.labmanagement.common.service.impl;

import com.thematrix.labmanagement.common.entity.Role;
import com.thematrix.labmanagement.common.entity.RoleAuthority;
import com.thematrix.labmanagement.common.service.RoleAuthorityService;
import com.thematrix.labmanagement.common.service.UserRoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * RoleServiceImpl 单元测试
 * 聚焦测试：updateRoleAuthority() 增量diff策略、saveRole() 重复校验、getUserRoleName() 空值处理
 */
@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleAuthorityService roleAuthorityService;
    @Mock
    private UserRoleService userRoleService;

    @Spy
    @InjectMocks
    private RoleServiceImpl roleService;

    // ==================== saveRole() ====================

    /**
     * 角色名称已存在时，saveRole 应抛出 RuntimeException
     */
    @Test
    void saveRole_duplicateName_throwsException() {
        Role existing = new Role();
        existing.setRoleName("管理员");
        doReturn(existing).when(roleService).getOne(any());

        Role newRole = new Role();
        newRole.setRoleName("管理员");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> roleService.saveRole(newRole));
        assertEquals("角色名称「管理员」已存在，请勿重复添加", ex.getMessage());

        verify(roleService, never()).save(any(Role.class));
    }

    // ==================== getUserRoleName() ====================

    /**
     * 传入空列表时，应返回空列表而不是抛异常
     */
    @Test
    void getUserRoleName_emptyList_returnsEmptyList() {
        List<String> result = roleService.getUserRoleName(Collections.emptyList());
        assertTrue(result.isEmpty());
    }

    /**
     * 传入 null 时，应返回空列表而不是抛异常
     */
    @Test
    void getUserRoleName_null_returnsEmptyList() {
        List<String> result = roleService.getUserRoleName(null);
        assertTrue(result.isEmpty());
    }

    // ==================== updateRoleAuthority() ====================

    /**
     * 传入空列表时，应清空该角色的所有权限
     */
    @Test
    void updateRoleAuthority_emptyList_clearsAll() {
        Long roleId = 1L;
        when(userRoleService.getUserIdsByRoleId(roleId)).thenReturn(Collections.emptyList());

        roleService.updateRoleAuthority(roleId, Collections.emptyList());

        // 验证调用了 remove 清空权限
        verify(roleAuthorityService).remove(any());
        // 验证没有调用增量 diff 相关方法
        verify(roleAuthorityService, never()).getAuthoritysByRoleIds(anyLong());
    }

    /**
     * 传入 null 时，也应清空该角色的所有权限
     */
    @Test
    void updateRoleAuthority_nullList_clearsAll() {
        Long roleId = 1L;
        when(userRoleService.getUserIdsByRoleId(roleId)).thenReturn(Collections.emptyList());

        roleService.updateRoleAuthority(roleId, null);

        verify(roleAuthorityService).remove(any());
    }

    /**
     * 增量 diff 策略：当前权限 [1,2,3]，目标权限 [2,3,4]
     * 应删除权限 1，添加权限 4，保留权限 2 和 3 不变
     */
    @Test
    void updateRoleAuthority_diffStrategy_addAndRemove() {
        Long roleId = 1L;
        List<Long> currentIds = Arrays.asList(1L, 2L, 3L);
        List<Long> targetIds = Arrays.asList(2L, 3L, 4L);

        when(roleAuthorityService.getAuthoritysByRoleIds(roleId)).thenReturn(currentIds);
        when(userRoleService.getUserIdsByRoleId(roleId)).thenReturn(Collections.emptyList());

        roleService.updateRoleAuthority(roleId, targetIds);

        // 验证删除了权限 1（不在目标列表中）
        verify(roleAuthorityService).deleteByRoleIdAndAuthorityId(roleId, 1L);
        // 验证没有删除权限 2 和 3（在目标列表中）
        verify(roleAuthorityService, never()).deleteByRoleIdAndAuthorityId(roleId, 2L);
        verify(roleAuthorityService, never()).deleteByRoleIdAndAuthorityId(roleId, 3L);
        // 验证添加了 1 个新权限（权限 4），因为 2 和 3 已存在
        verify(roleAuthorityService, times(1)).save(any(RoleAuthority.class));
    }

    /**
     * 增量 diff 策略：当前权限和目标权限完全相同时，不应做任何增删
     */
    @Test
    void updateRoleAuthority_noChange_noAddOrRemove() {
        Long roleId = 1L;
        List<Long> currentIds = Arrays.asList(1L, 2L);
        List<Long> targetIds = Arrays.asList(1L, 2L);

        when(roleAuthorityService.getAuthoritysByRoleIds(roleId)).thenReturn(currentIds);
        when(userRoleService.getUserIdsByRoleId(roleId)).thenReturn(Collections.emptyList());

        roleService.updateRoleAuthority(roleId, targetIds);

        // 没有删除
        verify(roleAuthorityService, never()).deleteByRoleIdAndAuthorityId(anyLong(), anyLong());
        // 没有添加
        verify(roleAuthorityService, never()).save(any(RoleAuthority.class));
    }
}

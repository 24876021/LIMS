package com.thematrix.labmanagement.personnel.service.impl;

import com.thematrix.labmanagement.common.entity.SysUser;
import com.thematrix.labmanagement.common.service.NotificationService;
import com.thematrix.labmanagement.common.service.SysUserService;
import com.thematrix.labmanagement.common.service.UserRoleService;
import com.thematrix.labmanagement.personnel.entity.Personnel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PersonnelServiceImpl 单元测试
 * 聚焦：addPersonnel 工号重复校验/save失败、updatePersonnel 工号重复/save失败/状态变更通知
 */
@ExtendWith(MockitoExtension.class)
class PersonnelServiceImplTest {

    @Mock
    private SysUserService sysUserService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRoleService userRoleService;

    @Spy
    @InjectMocks
    private PersonnelServiceImpl personnelService;

    // ==================== addPersonnel ====================

    @Test
    void addPersonnel_duplicateEmployeeNo_throwsException() {
        Personnel personnel = new Personnel();
        personnel.setEmployeeNo("EMP001");

        Personnel existing = new Personnel();
        existing.setEmployeeNo("EMP001");

        doReturn(existing).when(personnelService).getOne(any());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> personnelService.addPersonnel(personnel));
        assertTrue(ex.getMessage().contains("EMP001"));
        assertTrue(ex.getMessage().contains("已被其他人员使用"));
    }

    @Test
    void addPersonnel_saveFails_throwsException() {
        Personnel personnel = new Personnel();
        personnel.setEmployeeNo("EMP002");

        doReturn(null).when(personnelService).getOne(any());
        doReturn(false).when(personnelService).save(any(Personnel.class));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> personnelService.addPersonnel(personnel));
        assertEquals("新增人员失败", ex.getMessage());
    }

    @Test
    void addPersonnel_success_setsDefaultStatus() {
        Personnel personnel = new Personnel();
        personnel.setEmployeeNo("EMP003");

        doReturn(null).when(personnelService).getOne(any());
        doReturn(true).when(personnelService).save(any(Personnel.class));

        personnelService.addPersonnel(personnel);

        assertEquals("active", personnel.getStatus());
        assertNotNull(personnel.getCreateTime());
    }

    @Test
    void addPersonnel_withUserId_fillsFromSysUser() {
        Personnel personnel = new Personnel();
        personnel.setUserId(1L);
        // name/phone/email/gender 都为 null，应从 SysUser 填充

        SysUser user = new SysUser();
        user.setUserId(1L);
        user.setName("张三");
        user.setPhone("13800000000");
        user.setEmail("zhangsan@test.com");
        user.setSex("男");

        doReturn(true).when(personnelService).save(any(Personnel.class));
        when(sysUserService.getById(1L)).thenReturn(user);

        personnelService.addPersonnel(personnel);

        assertEquals("张三", personnel.getName());
        assertEquals("13800000000", personnel.getPhone());
        assertEquals("zhangsan@test.com", personnel.getEmail());
        assertEquals("男", personnel.getGender());
    }

    // ==================== updatePersonnel ====================

    @Test
    void updatePersonnel_duplicateEmployeeNo_throwsException() {
        Personnel existing = new Personnel();
        existing.setId(1L);
        existing.setStatus("active");

        Personnel personnel = new Personnel();
        personnel.setEmployeeNo("EMP001");

        Personnel conflict = new Personnel();
        conflict.setEmployeeNo("EMP001");

        doReturn(existing).when(personnelService).getById(1L);
        doReturn(conflict).when(personnelService).getOne(any());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> personnelService.updatePersonnel(1L, personnel));
        assertTrue(ex.getMessage().contains("EMP001"));
        assertTrue(ex.getMessage().contains("已被其他人员使用"));
    }

    @Test
    void updatePersonnel_updateFails_throwsException() {
        Personnel existing = new Personnel();
        existing.setId(1L);
        existing.setStatus("active");

        Personnel personnel = new Personnel();
        personnel.setEmployeeNo("EMP002");

        doReturn(existing).when(personnelService).getById(1L);
        doReturn(null).when(personnelService).getOne(any());
        doReturn(false).when(personnelService).updateById(any(Personnel.class));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> personnelService.updatePersonnel(1L, personnel));
        assertEquals("更新人员失败", ex.getMessage());
    }

    @Test
    void updatePersonnel_pendingToActive_sendsApprovalNotification() {
        Personnel existing = new Personnel();
        existing.setId(1L);
        existing.setUserId(2L);
        existing.setStatus("pending");

        Personnel personnel = new Personnel();
        personnel.setStatus("active");
        personnel.setEmployeeNo(null); // 不检查工号

        SysUser user = new SysUser();
        user.setUserId(2L);
        user.setName("李四");

        doReturn(existing).when(personnelService).getById(1L);
        doReturn(true).when(personnelService).updateById(any(Personnel.class));
        when(sysUserService.getById(2L)).thenReturn(user);

        personnelService.updatePersonnel(1L, personnel);

        verify(notificationService).sendNotification(
                isNull(), eq(2L), eq("认证审核通过"), contains("李四"), eq("success"));
    }

    @Test
    void updatePersonnel_pendingToRejected_sendsRejectionNotification() {
        Personnel existing = new Personnel();
        existing.setId(1L);
        existing.setUserId(2L);
        existing.setStatus("pending");

        Personnel personnel = new Personnel();
        personnel.setStatus("rejected");
        personnel.setEmployeeNo(null);

        SysUser user = new SysUser();
        user.setUserId(2L);
        user.setName("王五");

        doReturn(existing).when(personnelService).getById(1L);
        doReturn(true).when(personnelService).updateById(any(Personnel.class));
        when(sysUserService.getById(2L)).thenReturn(user);

        personnelService.updatePersonnel(1L, personnel);

        verify(notificationService).sendNotification(
                isNull(), eq(2L), eq("认证审核未通过"), contains("王五"), eq("warning"));
    }

    @Test
    void updatePersonnel_nonPendingStatus_noNotification() {
        Personnel existing = new Personnel();
        existing.setId(1L);
        existing.setUserId(2L);
        existing.setStatus("active");

        Personnel personnel = new Personnel();
        personnel.setStatus("active");
        personnel.setEmployeeNo(null);

        doReturn(existing).when(personnelService).getById(1L);
        doReturn(true).when(personnelService).updateById(any(Personnel.class));

        personnelService.updatePersonnel(1L, personnel);

        verifyNoInteractions(notificationService);
    }
}

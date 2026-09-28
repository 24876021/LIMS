package com.thematrix.labmanagement.equipment.service.impl;

import com.thematrix.labmanagement.common.service.NotificationService;
import com.thematrix.labmanagement.equipment.entity.Equipment;
import com.thematrix.labmanagement.equipment.entity.EquipmentMaintenanceRecord;
import com.thematrix.labmanagement.equipment.entity.EquipmentUsageRecord;
import com.thematrix.labmanagement.equipment.service.EquipmentMaintenanceRecordService;
import com.thematrix.labmanagement.equipment.service.EquipmentUsageRecordService;
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
 * EquipmentServiceImpl 单元测试
 * 聚焦：borrowEquipment/returnEquipment/submitMaintenance/completeMaintenance
 * 测试：设备不存在校验、状态流转、通知发送
 */
@ExtendWith(MockitoExtension.class)
class EquipmentServiceImplTest {

    @Mock
    private EquipmentUsageRecordService usageRecordService;

    @Mock
    private EquipmentMaintenanceRecordService maintenanceRecordService;

    @Mock
    private NotificationService notificationService;

    @Spy
    @InjectMocks
    private EquipmentServiceImpl equipmentService;

    // ==================== borrowEquipment ====================

    @Test
    void borrowEquipment_equipmentNotFound_throwsException() {
        doReturn(null).when(equipmentService).getById(1L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> equipmentService.borrowEquipment(1L, 2L, "实验用途"));
        assertEquals("设备不存在", ex.getMessage());
    }

    @Test
    void borrowEquipment_notNormalStatus_throwsException() {
        Equipment equipment = new Equipment();
        equipment.setId(1L);
        equipment.setStatus("borrowed");

        doReturn(equipment).when(equipmentService).getById(1L);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> equipmentService.borrowEquipment(1L, 2L, "实验用途"));
        assertEquals("设备当前不可借用", ex.getMessage());
    }

    @Test
    void borrowEquipment_success_changesStatusAndSendsNotification() {
        Equipment equipment = new Equipment();
        equipment.setId(1L);
        equipment.setName("显微镜");
        equipment.setStatus("normal");

        doReturn(equipment).when(equipmentService).getById(1L);
        doReturn(true).when(equipmentService).updateById(any(Equipment.class));
        when(usageRecordService.save(any(EquipmentUsageRecord.class))).thenReturn(true);

        equipmentService.borrowEquipment(1L, 2L, "实验用途");

        assertEquals("borrowed", equipment.getStatus());
        verify(notificationService).sendNotification(
                isNull(), eq(2L), eq("设备借用成功"), contains("显微镜"), eq("system"));
    }

    // ==================== returnEquipment ====================

    @Test
    void returnEquipment_equipmentNotFound_throwsException() {
        doReturn(null).when(equipmentService).getById(1L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> equipmentService.returnEquipment(1L, 2L));
        assertEquals("设备不存在", ex.getMessage());
    }

    @Test
    void returnEquipment_success_changesStatusAndUpdatesRecord() {
        Equipment equipment = new Equipment();
        equipment.setId(1L);
        equipment.setName("显微镜");
        equipment.setStatus("borrowed");

        EquipmentUsageRecord record = new EquipmentUsageRecord();
        record.setId(10L);
        record.setStatus("borrowing");

        doReturn(equipment).when(equipmentService).getById(1L);
        when(usageRecordService.getOne(any())).thenReturn(record);
        doReturn(true).when(equipmentService).updateById(any(Equipment.class));

        equipmentService.returnEquipment(1L, 2L);

        assertEquals("normal", equipment.getStatus());
        assertEquals("returned", record.getStatus());
        assertNotNull(record.getReturnTime());
        verify(notificationService).sendNotification(
                isNull(), eq(2L), eq("设备归还成功"), contains("显微镜"), eq("system"));
    }

    // ==================== submitMaintenance ====================

    @Test
    void submitMaintenance_equipmentNotFound_throwsException() {
        doReturn(null).when(equipmentService).getById(1L);

        EquipmentMaintenanceRecord record = new EquipmentMaintenanceRecord();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> equipmentService.submitMaintenance(1L, record));
        assertEquals("设备不存在", ex.getMessage());
    }

    @Test
    void submitMaintenance_success_changesStatusAndSendsNotification() {
        Equipment equipment = new Equipment();
        equipment.setId(1L);
        equipment.setName("离心机");
        equipment.setStatus("normal");

        EquipmentMaintenanceRecord record = new EquipmentMaintenanceRecord();
        record.setReporterId(2L);

        doReturn(equipment).when(equipmentService).getById(1L);
        doReturn(true).when(equipmentService).updateById(any(Equipment.class));
        when(maintenanceRecordService.save(any(EquipmentMaintenanceRecord.class))).thenReturn(true);

        equipmentService.submitMaintenance(1L, record);

        assertEquals("maintenance", equipment.getStatus());
        assertEquals("pending", record.getStatus());
        assertNotNull(record.getReportTime());
        verify(notificationService).sendNotification(
                isNull(), eq(2L), eq("设备报修申请已提交"), contains("离心机"), eq("info"));
    }

    @Test
    void submitMaintenance_nullReporterId_noNotification() {
        Equipment equipment = new Equipment();
        equipment.setId(1L);
        equipment.setName("离心机");
        equipment.setStatus("normal");

        EquipmentMaintenanceRecord record = new EquipmentMaintenanceRecord();
        record.setReporterId(null);

        doReturn(equipment).when(equipmentService).getById(1L);
        doReturn(true).when(equipmentService).updateById(any(Equipment.class));
        when(maintenanceRecordService.save(any(EquipmentMaintenanceRecord.class))).thenReturn(true);

        equipmentService.submitMaintenance(1L, record);

        verifyNoInteractions(notificationService);
    }

    // ==================== completeMaintenance ====================

    @Test
    void completeMaintenance_recordNotFound_throwsException() {
        when(maintenanceRecordService.getById(1L)).thenReturn(null);

        EquipmentMaintenanceRecord updateInfo = new EquipmentMaintenanceRecord();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> equipmentService.completeMaintenance(1L, updateInfo));
        assertEquals("维修记录不存在", ex.getMessage());
    }

    @Test
    void completeMaintenance_success_completesAndRestoresEquipment() {
        EquipmentMaintenanceRecord record = new EquipmentMaintenanceRecord();
        record.setId(1L);
        record.setEquipmentId(5L);
        record.setReporterId(2L);
        record.setStatus("pending");

        Equipment equipment = new Equipment();
        equipment.setId(5L);
        equipment.setName("光谱仪");
        equipment.setStatus("maintenance");

        EquipmentMaintenanceRecord updateInfo = new EquipmentMaintenanceRecord();
        updateInfo.setRepairResult("更换零件");
        updateInfo.setRepairPerson("张师傅");
        updateInfo.setRepairCost("500");

        when(maintenanceRecordService.getById(1L)).thenReturn(record);
        doReturn(equipment).when(equipmentService).getById(5L);
        doReturn(true).when(equipmentService).updateById(any(Equipment.class));

        equipmentService.completeMaintenance(1L, updateInfo);

        assertEquals("completed", record.getStatus());
        assertEquals("更换零件", record.getRepairResult());
        assertEquals("张师傅", record.getRepairPerson());
        assertNotNull(record.getCompleteTime());
        assertEquals("normal", equipment.getStatus());
        verify(notificationService).sendNotification(
                isNull(), eq(2L), eq("设备维修完成"), contains("光谱仪"), eq("success"));
    }
}

package com.thematrix.labmanagement.resources.service.impl;

import com.thematrix.labmanagement.common.service.NotificationService;
import com.thematrix.labmanagement.resources.entity.PurchaseRecord;
import com.thematrix.labmanagement.resources.entity.Resource;
import com.thematrix.labmanagement.resources.entity.UsageRecord;
import com.thematrix.labmanagement.resources.service.PurchaseRecordService;
import com.thematrix.labmanagement.resources.service.UsageRecordService;
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
 * ResourceServiceImpl 单元测试
 * 聚焦：purchase/consume 的物资校验、库存校验、save返回值检查
 */
@ExtendWith(MockitoExtension.class)
class ResourceServiceImplTest {

    @Mock
    private PurchaseRecordService purchaseRecordService;

    @Mock
    private UsageRecordService usageRecordService;

    @Mock
    private NotificationService notificationService;

    @Spy
    @InjectMocks
    private ResourceServiceImpl resourceService;

    // ==================== purchase ====================

    @Test
    void purchase_resourceNotFound_throwsException() {
        doReturn(null).when(resourceService).getById(1L);

        PurchaseRecord record = new PurchaseRecord();
        record.setQuantity(10);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> resourceService.purchase(1L, record));
        assertEquals("物资不存在", ex.getMessage());
    }

    @Test
    void purchase_saveFails_throwsException() {
        Resource resource = new Resource();
        resource.setId(1L);
        resource.setQuantity(5);
        resource.setName("试管");

        doReturn(resource).when(resourceService).getById(1L);
        doReturn(true).when(resourceService).updateById(any(Resource.class));
        when(purchaseRecordService.save(any(PurchaseRecord.class))).thenReturn(false);

        PurchaseRecord record = new PurchaseRecord();
        record.setQuantity(10);
        record.setPurchaserId(null); // 不触发通知

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> resourceService.purchase(1L, record));
        assertEquals("入库失败", ex.getMessage());
    }

    @Test
    void purchase_success_increasesQuantityAndSendsNotification() {
        Resource resource = new Resource();
        resource.setId(1L);
        resource.setQuantity(5);
        resource.setName("试管");
        resource.setUnit("个");

        doReturn(resource).when(resourceService).getById(1L);
        doReturn(true).when(resourceService).updateById(any(Resource.class));
        when(purchaseRecordService.save(any(PurchaseRecord.class))).thenReturn(true);

        PurchaseRecord record = new PurchaseRecord();
        record.setQuantity(10);
        record.setPurchaserId(2L);

        resourceService.purchase(1L, record);

        // 验证库存增加了
        assertEquals(15, resource.getQuantity());
        verify(notificationService).sendNotification(
                isNull(), eq(2L), eq("采购入库成功"), contains("试管"), eq("info"));
    }

    // ==================== consume ====================

    @Test
    void consume_resourceNotFound_throwsException() {
        doReturn(null).when(resourceService).getById(1L);

        UsageRecord record = new UsageRecord();
        record.setQuantity(1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> resourceService.consume(1L, record));
        assertEquals("物资不存在", ex.getMessage());
    }

    @Test
    void consume_insufficientStock_throwsException() {
        Resource resource = new Resource();
        resource.setId(1L);
        resource.setQuantity(3);

        doReturn(resource).when(resourceService).getById(1L);

        UsageRecord record = new UsageRecord();
        record.setQuantity(5);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> resourceService.consume(1L, record));
        assertEquals("库存不足", ex.getMessage());
    }

    @Test
    void consume_saveFails_throwsException() {
        Resource resource = new Resource();
        resource.setId(1L);
        resource.setQuantity(10);
        resource.setName("试管");

        doReturn(resource).when(resourceService).getById(1L);
        doReturn(true).when(resourceService).updateById(any(Resource.class));
        when(usageRecordService.save(any(UsageRecord.class))).thenReturn(false);

        UsageRecord record = new UsageRecord();
        record.setQuantity(3);
        record.setUserId(null); // 不触发通知

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> resourceService.consume(1L, record));
        assertEquals("领用失败", ex.getMessage());
    }

    @Test
    void consume_success_decreasesQuantityAndSendsNotification() {
        Resource resource = new Resource();
        resource.setId(1L);
        resource.setQuantity(10);
        resource.setName("试管");
        resource.setUnit("个");

        doReturn(resource).when(resourceService).getById(1L);
        doReturn(true).when(resourceService).updateById(any(Resource.class));
        when(usageRecordService.save(any(UsageRecord.class))).thenReturn(true);

        UsageRecord record = new UsageRecord();
        record.setQuantity(3);
        record.setUserId(2L);

        resourceService.consume(1L, record);

        // 验证库存扣减了
        assertEquals(7, resource.getQuantity());
        verify(notificationService).sendNotification(
                isNull(), eq(2L), eq("物资领用成功"), contains("试管"), eq("info"));
    }
}

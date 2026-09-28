package com.thematrix.labmanagement.resources.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thematrix.labmanagement.common.service.NotificationService;
import com.thematrix.labmanagement.resources.entity.PurchaseRecord;
import com.thematrix.labmanagement.resources.entity.Resource;
import com.thematrix.labmanagement.resources.entity.UsageRecord;
import com.thematrix.labmanagement.resources.mapper.ResourceMapper;
import com.thematrix.labmanagement.resources.service.PurchaseRecordService;
import com.thematrix.labmanagement.resources.service.ResourceService;
import com.thematrix.labmanagement.resources.service.UsageRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ResourceServiceImpl extends ServiceImpl<ResourceMapper, Resource> implements ResourceService {

    @Autowired
    private PurchaseRecordService purchaseRecordService;

    @Autowired
    private UsageRecordService usageRecordService;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void purchase(Long resourceId, PurchaseRecord record) {
        Resource resource = this.getById(resourceId);
        if (resource == null) {
            throw new IllegalArgumentException("物资不存在");
        }

        // 库存增加
        resource.setQuantity(resource.getQuantity() + record.getQuantity());
        this.updateById(resource);

        // 创建采购记录
        record.setResourceId(resourceId);
        record.setPurchaseTime(LocalDateTime.now());
        boolean saved = purchaseRecordService.save(record);
        if (!saved) {
            throw new RuntimeException("入库失败");
        }

        // 通知采购人
        if (record.getPurchaserId() != null) {
            notificationService.sendNotification(null, record.getPurchaserId(),
                    "采购入库成功",
                    "物资《" + resource.getName() + "》采购入库成功，数量 +" + record.getQuantity()
                            + (resource.getUnit() != null ? resource.getUnit() : "个")
                            + "，当前库存 " + resource.getQuantity()
                            + (resource.getUnit() != null ? resource.getUnit() : "个"),
                    "info");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void consume(Long resourceId, UsageRecord record) {
        Resource resource = this.getById(resourceId);
        if (resource == null) {
            throw new IllegalArgumentException("物资不存在");
        }
        if (resource.getQuantity() < record.getQuantity()) {
            throw new IllegalStateException("库存不足");
        }

        // 库存扣减
        resource.setQuantity(resource.getQuantity() - record.getQuantity());
        this.updateById(resource);

        // 创建领用记录
        record.setResourceId(resourceId);
        record.setUsageTime(LocalDateTime.now());
        boolean saved = usageRecordService.save(record);
        if (!saved) {
            throw new RuntimeException("领用失败");
        }

        // 通知领用人
        if (record.getUserId() != null) {
            notificationService.sendNotification(null, record.getUserId(),
                    "物资领用成功",
                    "您已成功领用物资《" + resource.getName() + "》"
                            + record.getQuantity() + (resource.getUnit() != null ? resource.getUnit() : "个")
                            + "，当前库存剩余 " + resource.getQuantity()
                            + (resource.getUnit() != null ? resource.getUnit() : "个"),
                    "info");
        }
    }

    @Override
    public List<Object> getRecords(Long resourceId) {
        List<PurchaseRecord> purchases = purchaseRecordService.list(
                new LambdaQueryWrapper<PurchaseRecord>().eq(PurchaseRecord::getResourceId, resourceId));
        List<UsageRecord> usages = usageRecordService.list(
                new LambdaQueryWrapper<UsageRecord>().eq(UsageRecord::getResourceId, resourceId));
        List<Object> allRecords = new ArrayList<>();
        allRecords.addAll(purchases);
        allRecords.addAll(usages);
        return allRecords;
    }
}

package com.thematrix.labmanagement.resources.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thematrix.labmanagement.resources.entity.PurchaseRecord;
import com.thematrix.labmanagement.resources.entity.Resource;
import com.thematrix.labmanagement.resources.entity.UsageRecord;

import java.util.List;

public interface ResourceService extends IService<Resource> {

    /**
     * 采购入库（库存增加 + 创建采购记录 + 通知）
     */
    void purchase(Long resourceId, PurchaseRecord record);

    /**
     * 物资领用（库存扣减 + 创建使用记录 + 通知）
     */
    void consume(Long resourceId, UsageRecord record);

    /**
     * 获取资源的采购和使用记录
     */
    List<Object> getRecords(Long resourceId);
}

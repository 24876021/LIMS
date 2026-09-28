package com.thematrix.labmanagement.safety.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thematrix.labmanagement.common.service.NotificationService;
import com.thematrix.labmanagement.safety.entity.SafetyInspection;
import com.thematrix.labmanagement.safety.mapper.SafetyInspectionMapper;
import com.thematrix.labmanagement.safety.service.SafetyInspectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SafetyInspectionServiceImpl extends ServiceImpl<SafetyInspectionMapper, SafetyInspection>
        implements SafetyInspectionService {

    @Autowired
    private NotificationService notificationService;

    @Override
    public boolean saveInspection(SafetyInspection inspection) {
        inspection.setCreateTime(java.time.LocalDateTime.now());
        return save(inspection);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInspection(Long id, SafetyInspection inspection) {
        String result = inspection.getResult();
        boolean resolved = result != null && (result.contains("已整改") || result.contains("已完成")
                || result.contains("整改完成") || result.contains("completed") || result.contains("resolved")
                || "passed".equals(result));

        SafetyInspection entity = this.getById(id);
        if (entity != null) {
            entity.setResult(result);
            this.updateById(entity);

            if (resolved) {
                SafetyInspection updated = this.getById(id);
                if (updated != null && updated.getInspectorId() != null) {
                    notificationService.sendNotification(null, updated.getInspectorId(),
                            "安全检查整改完成",
                            "安全检查「" + (updated.getTitle() != null ? updated.getTitle() : "未知") + "」已整改完成。",
                            "success");
                }
            }
        }
    }
}

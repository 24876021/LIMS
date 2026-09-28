package com.thematrix.labmanagement.safety.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thematrix.labmanagement.common.service.NotificationService;
import com.thematrix.labmanagement.safety.entity.SafetyIncident;
import com.thematrix.labmanagement.safety.mapper.SafetyIncidentMapper;
import com.thematrix.labmanagement.safety.service.SafetyIncidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SafetyIncidentServiceImpl extends ServiceImpl<SafetyIncidentMapper, SafetyIncident>
        implements SafetyIncidentService {

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addIncident(SafetyIncident incident) {
        incident.setCreateTime(LocalDateTime.now());
        boolean saved = this.save(incident);
        if (!saved) {
            throw new RuntimeException("新增失败");
        }

        if (incident.getReporterId() != null) {
            notificationService.sendNotification(null, incident.getReporterId(),
                    "安全事故已记录",
                    "您上报的安全事故已记录，管理员将跟进处理。事故描述："
                            + (incident.getDescription() != null ? incident.getDescription() : "（无描述）"),
                    "warning");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateIncident(Long id, SafetyIncident incident) {
        String status = incident.getStatus();
        boolean resolved = status != null && (status.contains("已解决") || status.contains("resolved")
                || status.contains("closed") || status.contains("已处理") || status.contains("处理完成"));

        incident.setId(id);
        boolean success = this.updateById(incident);
        if (!success) {
            throw new RuntimeException("更新失败");
        }

        if (resolved) {
            SafetyIncident updated = this.getById(id);
            if (updated != null && updated.getReporterId() != null) {
                String desc = updated.getDescription() != null
                        ? (updated.getDescription().length() > 30
                                ? updated.getDescription().substring(0, 30) + "..."
                                : updated.getDescription())
                        : "无描述";
                notificationService.sendNotification(null, updated.getReporterId(),
                        "安全事故已解决",
                        "您上报的安全事故「" + desc + "」已标记为已解决。",
                        "success");
            }
        }
    }
}

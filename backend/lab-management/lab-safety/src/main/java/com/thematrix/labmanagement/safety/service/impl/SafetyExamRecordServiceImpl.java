package com.thematrix.labmanagement.safety.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.thematrix.labmanagement.common.entity.SysUser;
import com.thematrix.labmanagement.common.service.NotificationService;
import com.thematrix.labmanagement.common.service.SysUserService;
import com.thematrix.labmanagement.safety.entity.SafetyExamRecord;
import com.thematrix.labmanagement.safety.entity.SafetyTraining;
import com.thematrix.labmanagement.safety.mapper.SafetyExamRecordMapper;
import com.thematrix.labmanagement.safety.service.SafetyExamRecordService;
import com.thematrix.labmanagement.safety.service.SafetyTrainingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SafetyExamRecordServiceImpl extends ServiceImpl<SafetyExamRecordMapper, SafetyExamRecord>
        implements SafetyExamRecordService {

    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SafetyTrainingService trainingService;
    @Autowired
    private NotificationService notificationService;

    @Override
    public void submitExam(String username, Map<String, Object> params) {
        // 根据用户名查找 userId
        SysUser user = sysUserService.getUserByAccount(username);
        if (user == null) {
            throw new RuntimeException("未找到当前用户信息");
        }
        Long userId = user.getUserId();

        Integer score = Integer.valueOf(params.get("score").toString());
        Integer totalScore = Integer.valueOf(params.get("totalScore").toString());
        Boolean passed = (Boolean) params.get("passed");
        Long trainingId = params.get("trainingId") != null
                ? Long.valueOf(params.get("trainingId").toString()) : null;

        SafetyExamRecord record = new SafetyExamRecord();
        record.setUserId(userId);
        record.setTrainingId(trainingId);
        record.setScore(score);
        record.setTotalScore(totalScore);
        record.setPassed(passed);
        record.setStatus(1); // 0=待完成，1=已完成
        record.setExamTime(LocalDateTime.now());

        if (!save(record)) {
            throw new RuntimeException("提交失败");
        }

        // 发送考试结果通知
        if (Boolean.TRUE.equals(passed)) {
            notificationService.sendNotification(null, userId,
                    "安全考试通过",
                    "您本次安全考试已通过！得分：" + score + "/" + totalScore,
                    "success");
        } else {
            notificationService.sendNotification(null, userId,
                    "安全考试未通过",
                    "您本次安全考试未通过，得分：" + score + "/" + totalScore + "，请加强学习后重新参加考试。",
                    "warning");
        }
    }

    @Override
    public List<Long> getMyCompletedTrainingIds(String username) {
        SysUser user = sysUserService.getUserByAccount(username);
        Long userId = user != null ? user.getUserId() : null;

        List<SafetyExamRecord> records = list(
                new LambdaQueryWrapper<SafetyExamRecord>()
                        .eq(SafetyExamRecord::getUserId, userId)
        );
        return records.stream()
                .filter(r -> r.getStatus() != null && r.getStatus() == 1)
                .map(SafetyExamRecord::getTrainingId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public Map<Long, Long> getExamStats() {
        List<SafetyExamRecord> allRecords = list(
                new LambdaQueryWrapper<SafetyExamRecord>().eq(SafetyExamRecord::getStatus, 1)
        );
        return allRecords.stream()
                .filter(r -> r.getTrainingId() != null)
                .collect(Collectors.groupingBy(SafetyExamRecord::getTrainingId, Collectors.counting()));
    }

    @Override
    public List<Map<String, Object>> getUserExamRecordsWithTrainingName(Long userId) {
        List<SafetyExamRecord> records = list(
                new LambdaQueryWrapper<SafetyExamRecord>().eq(SafetyExamRecord::getUserId, userId)
        );
        return records.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("userId", r.getUserId());
            map.put("trainingId", r.getTrainingId());
            map.put("score", r.getScore());
            map.put("totalScore", r.getTotalScore());
            map.put("passed", r.getPassed());
            map.put("status", r.getStatus());
            map.put("examTime", r.getExamTime() != null ? r.getExamTime().toString() : null);
            if (r.getTrainingId() != null) {
                SafetyTraining training = trainingService.getById(r.getTrainingId());
                map.put("trainingName", training != null ? training.getTitle() : "未知培训");
            } else {
                map.put("trainingName", "未知培训");
            }
            return map;
        }).collect(Collectors.toList());
    }
}

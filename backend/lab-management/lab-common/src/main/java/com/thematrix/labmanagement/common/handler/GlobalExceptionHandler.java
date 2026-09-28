package com.thematrix.labmanagement.common.handler;

import com.thematrix.labmanagement.common.utils.Result;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 捕获各模块未处理的异常，返回友好提示
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 外键约束名 → 中文友好提示映射
     */
    private static final Map<String, String> FK_MESSAGES = new LinkedHashMap<>();
    static {
        // 用户-角色
        FK_MESSAGES.put("fk_user_role_user", "所选用户不存在，请刷新后重试");
        FK_MESSAGES.put("fk_user_role_role", "所选角色不存在，请刷新后重试");
        // 角色-权限
        FK_MESSAGES.put("fk_role_authority_role", "所选角色不存在，请刷新后重试");
        FK_MESSAGES.put("fk_role_authority_authority", "所选权限不存在，请刷新后重试");
        // 部门
        FK_MESSAGES.put("fk_departments_parent", "上级部门不存在，请刷新后重试");
        FK_MESSAGES.put("fk_departments_leader", "所选负责人不存在，请刷新后重试");
        FK_MESSAGES.put("fk_sys_user_department", "所属部门不存在，请刷新后重试");
        // 人员
        FK_MESSAGES.put("fk_personnel_user", "关联的系统用户不存在，请刷新后重试");
        FK_MESSAGES.put("fk_personnel_department", "所属部门不存在，请刷新后重试");
        // 设备
        FK_MESSAGES.put("fk_equipment_usage_equipment", "设备不存在，请刷新页面后重试");
        FK_MESSAGES.put("fk_equipment_usage_user", "借用用户不存在，请刷新页面后重试");
        FK_MESSAGES.put("fk_equipment_maintenance_equipment", "设备不存在，请刷新页面后重试");
        FK_MESSAGES.put("fk_equipment_maintenance_reporter", "当前登录用户信息异常，请重新登录后再试");
        // 预约
        FK_MESSAGES.put("fk_reservation_lab", "预约的实验室不存在，请刷新后重试");
        FK_MESSAGES.put("fk_reservation_user", "预约用户不存在，请刷新后重试");
        FK_MESSAGES.put("fk_reservation_approver", "审批人不存在，请刷新后重试");
        // 物资-采购/领用
        FK_MESSAGES.put("fk_purchase_resource", "物资不存在，请刷新页面后重试");
        FK_MESSAGES.put("fk_purchase_purchaser", "采购人不存在，请刷新后重试");
        FK_MESSAGES.put("fk_usage_resource", "物资不存在，请刷新页面后重试");
        FK_MESSAGES.put("fk_usage_user", "领用人不存在，请刷新后重试");
        // 报告
        FK_MESSAGES.put("fk_report_user", "提交用户不存在，请刷新后重试");
        FK_MESSAGES.put("fk_report_reviewer", "审批人不存在，请刷新后重试");
        // 消息通知
        FK_MESSAGES.put("fk_notification_sender", "发送者不存在，请刷新后重试");
        FK_MESSAGES.put("fk_notification_receiver", "接收者不存在，请刷新后重试");
        // 安全
        FK_MESSAGES.put("fk_safety_exam_user", "考试用户不存在，请刷新后重试");
        FK_MESSAGES.put("fk_safety_exam_training", "关联的培训不存在，请刷新后重试");
        FK_MESSAGES.put("fk_safety_question_training", "关联的培训不存在，请刷新后重试");
        FK_MESSAGES.put("fk_safety_inspection_inspector", "检查人不存在，请刷新后重试");
        FK_MESSAGES.put("fk_safety_incident_reporter", "报告人不存在，请刷新后重试");
        FK_MESSAGES.put("fk_safety_training_creator", "创建人不存在，请刷新后重试");
        // 考勤
        FK_MESSAGES.put("fk_attendance_record_personnel", "考勤人员不存在，请刷新后重试");
        FK_MESSAGES.put("fk_attendance_record_activity", "考勤活动不存在，请刷新后重试");
        FK_MESSAGES.put("fk_attendance_activity_creator", "创建人不存在，请刷新后重试");
    }

    /**
     * 处理数据库唯一约束冲突（DuplicateKeyException）
     * 返回友好提示，避免将数据库错误直接抛给前端
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result handleDuplicateKeyException(DuplicateKeyException ex) {
        String msg = ex.getMostSpecificCause().getMessage();
        // 尝试从错误信息中提取重复的值
        // MySQL 错误信息格式：Duplicate entry 'xxx' for key 'table.uk_xxx'
        String friendlyMsg = "数据重复，请检查输入内容";
        if (msg != null) {
            int entryStart = msg.indexOf("Duplicate entry '");
            if (entryStart >= 0) {
                int entryEnd = msg.indexOf("'", entryStart + 16);
                if (entryEnd > entryStart + 16) {
                    String duplicateValue = msg.substring(entryStart + 15, entryEnd);
                    friendlyMsg = "数据重复：值「" + duplicateValue + "」已存在，请勿重复提交";
                }
            }
        }
        return Result.error(friendlyMsg);
    }

    /**
     * 处理外键约束违反（DataIntegrityViolationException）
     * 涵盖两种场景：
     * 1. 插入/更新时引用了不存在的外键值（child row violation）
     * 2. 删除/更新父记录时子记录仍引用它（parent row violation）
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String msg = ex.getMostSpecificCause().getMessage();
        if (msg == null) {
            return Result.error("数据操作失败，存在关联约束限制");
        }

        // 从 MySQL 错误信息中提取约束名
        String constraintName = extractConstraintName(msg);

        if (constraintName != null && FK_MESSAGES.containsKey(constraintName)) {
            String friendlyMsg = FK_MESSAGES.get(constraintName);
            // 区分是"引用不存在"还是"删除被引用的记录"
            if (msg.contains("Cannot delete or update a parent row")) {
                return Result.error(friendlyMsg + "（该记录正被其他数据引用，无法删除）");
            }
            return Result.error(friendlyMsg);
        }

        // 未能匹配具体约束名，返回通用提示
        if (msg.contains("Cannot delete or update a parent row")) {
            return Result.error("该记录正被其他数据引用，无法删除或修改");
        }
        if (msg.contains("Cannot add or update a child row")) {
            return Result.error("关联数据不存在，请检查输入的关联ID是否正确");
        }

        return Result.error("数据操作失败，存在关联约束限制");
    }

    /**
     * 从 MySQL 外键错误信息中提取约束名
     * 例如：CONSTRAINT `fk_equipment_maintenance_reporter` → fk_equipment_maintenance_reporter
     */
    private String extractConstraintName(String msg) {
        int idx = msg.indexOf("CONSTRAINT `");
        if (idx < 0) return null;
        int start = idx + "CONSTRAINT `".length();
        int end = msg.indexOf("`", start);
        if (end <= start) return null;
        return msg.substring(start, end);
    }

    /**
     * 处理业务异常（RuntimeException 及其子类）
     * Service 层抛出的 RuntimeException 统一捕获，返回友好的 Result.error 格式
     */
    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException ex) {
        return Result.error(ex.getMessage() != null ? ex.getMessage() : "操作失败，请稍后再试");
    }

    /**
     * 兜底异常处理（Exception）
     * 捕获所有未被上述具体处理器覆盖的异常
     */
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception ex) {
        return Result.error("系统内部错误：" + (ex.getMessage() != null ? ex.getMessage() : "未知错误"));
    }
}

package com.thematrix.labmanagement.safety.controller;

import com.thematrix.labmanagement.common.utils.Result;
import com.thematrix.labmanagement.safety.entity.*;
import com.thematrix.labmanagement.safety.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 安全管理控制器（重构版：业务逻辑已下沉到 Service 层）
 */
@RestController
@Api(tags = "安全管理相关接口")
@RequestMapping("/api/safety")
public class SafetyController {

    @Autowired private SafetyRegulationService regulationService;
    @Autowired private SafetyTrainingService trainingService;
    @Autowired private SafetyQuestionService questionService;
    @Autowired private SafetyExamRecordService examRecordService;
    @Autowired private SafetyInspectionService inspectionService;
    @Autowired private SafetyIncidentService incidentService;

    // ====== 安全制度 ======
    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('safety:get')")
    @GetMapping("/regulations")
    @ApiOperation("安全制度列表")
    public Result getRegulations() { return Result.success(regulationService.list()); }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('safety:set')")
    @PostMapping("/regulations")
    @ApiOperation("新增制度")
    public Result addRegulation(@RequestBody SafetyRegulation regulation) {
        return regulationService.saveRegulation(regulation) ? Result.success("新增成功") : Result.error("新增失败");
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('safety:set')")
    @PutMapping("/regulations/{id}")
    @ApiOperation("更新制度")
    public Result updateRegulation(@PathVariable Long id, @RequestBody SafetyRegulation regulation) {
        return regulationService.updateRegulation(id, regulation) ? Result.success("更新成功") : Result.error("更新失败");
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('safety:remove')")
    @DeleteMapping("/regulations/{id}")
    @ApiOperation("删除制度")
    public Result deleteRegulation(@PathVariable Long id) {
        return regulationService.removeById(id) ? Result.success("删除成功") : Result.error("删除失败");
    }

    // ====== 安全培训 ======
    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('safety:get')")
    @GetMapping("/trainings")
    @ApiOperation("培训列表")
    public Result getTrainings() { return Result.success(trainingService.list()); }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('safety:set')")
    @PostMapping("/trainings")
    @ApiOperation("新增培训")
    public Result addTraining(@RequestBody SafetyTraining training) {
        return trainingService.saveTraining(training) ? Result.success("新增成功") : Result.error("新增失败");
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('safety:set')")
    @PutMapping("/trainings/{id}")
    @ApiOperation("更新培训")
    public Result updateTraining(@PathVariable Long id, @RequestBody SafetyTraining training) {
        return trainingService.updateTraining(id, training) ? Result.success("更新成功") : Result.error("更新失败");
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('safety:remove')")
    @DeleteMapping("/trainings/{id}")
    @ApiOperation("删除培训")
    public Result deleteTraining(@PathVariable Long id) {
        return trainingService.removeById(id) ? Result.success("删除成功") : Result.error("删除失败");
    }

    // ====== 安全题目 ======
    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('safety:get')")
    @GetMapping("/questions")
    @ApiOperation("题目列表")
    public Result getQuestions(@RequestParam(required = false) Long trainingId) {
        if (trainingId != null) {
            return Result.success(questionService.getByTrainingId(trainingId));
        }
        return Result.success(questionService.list());
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('safety:set')")
    @PostMapping("/questions")
    @ApiOperation("新增题目")
    public Result addQuestion(@RequestBody SafetyQuestion question) {
        return questionService.save(question) ? Result.success("新增成功") : Result.error("新增失败");
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('safety:remove')")
    @DeleteMapping("/questions/{id}")
    @ApiOperation("删除题目")
    public Result deleteQuestion(@PathVariable Long id) {
        return questionService.removeById(id) ? Result.success("删除成功") : Result.error("删除失败");
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('safety:set')")
    @PostMapping("/questions/{id}/update")
    @ApiOperation("更新题目")
    public Result updateQuestion(@PathVariable Long id, @RequestBody SafetyQuestion question) {
        question.setId(id);
        return questionService.updateById(question) ? Result.success("更新成功") : Result.error("更新失败");
    }

    // ====== 安全考试 ======
    @PostMapping("/exam/submit")
    @ApiOperation("提交答案")
    public Result submitExam(@RequestBody Map<String, Object> params) {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        examRecordService.submitExam(username, params);
        return Result.success("提交成功");
    }

    @GetMapping("/exam/my-records")
    @ApiOperation("获取当前用户的考试记录")
    public Result getMyExamRecords() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.success(examRecordService.getMyCompletedTrainingIds(username));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/exam-records/stats")
    @ApiOperation("获取各培训完成人数统计")
    public Result getExamStats() {
        return Result.success(examRecordService.getExamStats());
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('safety:get')")
    @GetMapping("/exam-records/user/{userId}")
    @ApiOperation("获取指定用户的考试记录")
    public Result getUserExamRecords(@PathVariable Long userId) {
        return Result.success(examRecordService.getUserExamRecordsWithTrainingName(userId));
    }

    // ====== 安全检查 ======
    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('safety:get')")
    @GetMapping("/inspections")
    @ApiOperation("检查列表")
    public Result getInspections() { return Result.success(inspectionService.list()); }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('safety:set')")
    @PostMapping("/inspections")
    @ApiOperation("新增检查")
    public Result addInspection(@RequestBody SafetyInspection inspection) {
        return inspectionService.saveInspection(inspection) ? Result.success("新增成功") : Result.error("新增失败");
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('safety:set')")
    @PutMapping("/inspections/{id}")
    @ApiOperation("更新检查")
    public Result updateInspection(@PathVariable Long id, @RequestBody SafetyInspection inspection) {
        inspectionService.updateInspection(id, inspection);
        return Result.success("更新成功");
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('safety:remove')")
    @DeleteMapping("/inspections/{id}")
    @ApiOperation("删除检查")
    public Result deleteInspection(@PathVariable Long id) {
        return inspectionService.removeById(id) ? Result.success("删除成功") : Result.error("删除失败");
    }

    // ====== 安全事故 ======
    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('safety:get')")
    @GetMapping("/incidents")
    @ApiOperation("事故列表")
    public Result getIncidents() { return Result.success(incidentService.list()); }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('safety:set')")
    @PostMapping("/incidents")
    @ApiOperation("新增事故")
    public Result addIncident(@RequestBody SafetyIncident incident) {
        incidentService.addIncident(incident);
        return Result.success("新增成功");
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('safety:set')")
    @PutMapping("/incidents/{id}")
    @ApiOperation("更新事故")
    public Result updateIncident(@PathVariable Long id, @RequestBody SafetyIncident incident) {
        incidentService.updateIncident(id, incident);
        return Result.success("更新成功");
    }

    @PreAuthorize("hasAuthority('resource:all') || hasAuthority('safety:remove')")
    @DeleteMapping("/incidents/{id}")
    @ApiOperation("删除事故")
    public Result deleteIncident(@PathVariable Long id) {
        return incidentService.removeById(id) ? Result.success("删除成功") : Result.error("删除失败");
    }
}

package com.hero.quest.controller;

import com.hero.quest.dto.*;
import com.hero.quest.entity.Admin;
import com.hero.quest.entity.EmailConfig;
import com.hero.quest.entity.EmailReminderRule;
import com.hero.quest.entity.Task;
import com.hero.quest.service.AdminService;
import com.hero.quest.service.EmailService;
import com.hero.quest.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final TaskService taskService;
    private final AdminService adminService;
    private final EmailService emailService;

    public AdminController(TaskService taskService, AdminService adminService, EmailService emailService) {
        this.taskService = taskService;
        this.adminService = adminService;
        this.emailService = emailService;
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> getCurrentAdmin() {
        Admin admin = adminService.getCurrentAdmin();
        return ApiResponse.success(Map.of(
                "id", admin.getId(),
                "username", admin.getUsername(),
                "email", admin.getEmail() != null ? admin.getEmail() : ""
        ));
    }

    @PostMapping("/tasks")
    public ApiResponse<Task> createTask(@Valid @RequestBody TaskCreateRequest request) {
        return ApiResponse.success("任务创建成功", taskService.createTask(request));
    }

    @GetMapping("/tasks")
    public ApiResponse<List<Task>> getAllTasks(
            @RequestParam(required = false) Integer status) {
        if (status != null) {
            return ApiResponse.success(taskService.getTasksByStatus(status));
        }
        return ApiResponse.success(taskService.getAllTasks());
    }

    @GetMapping("/tasks/{taskId}")
    public ApiResponse<TaskDetailResponse> getTaskDetail(@PathVariable Long taskId) {
        return ApiResponse.success(taskService.getTaskDetail(taskId));
    }

    @PostMapping("/tasks/{taskId}/members/{memberId}/approve")
    public ApiResponse<Void> approveMember(@PathVariable Long taskId,
                                           @PathVariable Long memberId) {
        taskService.approveTaskMember(taskId, memberId);
        return ApiResponse.success("审核通过", null);
    }

    @PostMapping("/tasks/{taskId}/members/{memberId}/approve-only")
    public ApiResponse<Void> approveMemberOnly(@PathVariable Long taskId,
                                               @PathVariable Long memberId) {
        taskService.approveMemberOnly(taskId, memberId);
        return ApiResponse.success("审核通过", null);
    }

    @PostMapping("/tasks/{taskId}/start")
    public ApiResponse<Void> startTask(@PathVariable Long taskId) {
        taskService.startTask(taskId);
        return ApiResponse.success("任务已开始", null);
    }

    @PostMapping("/tasks/{taskId}/members/{memberId}/reject")
    public ApiResponse<Void> rejectMember(@PathVariable Long taskId,
                                          @PathVariable Long memberId) {
        taskService.rejectTaskMember(taskId, memberId);
        return ApiResponse.success("已拒绝", null);
    }

    @DeleteMapping("/tasks/{taskId}/members/{memberId}")
    public ApiResponse<Void> removeMember(@PathVariable Long taskId,
                                          @PathVariable Long memberId) {
        taskService.removeMemberFromTask(taskId, memberId);
        return ApiResponse.success("成员已移除", null);
    }

    @PostMapping("/tasks/{taskId}/assign")
    public ApiResponse<Void> assignMember(@PathVariable Long taskId,
                                          @Valid @RequestBody TaskClaimRequest request) {
        taskService.assignMemberDirectly(taskId, request);
        return ApiResponse.success("分配成功", null);
    }

    @PostMapping("/tasks/{taskId}/complete")
    public ApiResponse<Void> completeTask(@PathVariable Long taskId) {
        taskService.completeTask(taskId);
        return ApiResponse.success("任务已完成", null);
    }

    @PostMapping("/email/config")
    public ApiResponse<EmailConfig> saveEmailConfig(@Valid @RequestBody EmailConfigRequest request) {
        EmailConfig config = emailService.saveEmailConfig(request.getMailUsername(), request.getMailPassword());
        return ApiResponse.success("邮件配置保存成功", config);
    }

    @GetMapping("/email/config")
    public ApiResponse<EmailConfig> getEmailConfig() {
        return ApiResponse.success(emailService.getActiveEmailConfig());
    }

    @PostMapping("/email/test")
    public ApiResponse<Void> testEmail(@Valid @RequestBody EmailTestRequest request) {
        emailService.testEmailSending(request.getToEmail());
        return ApiResponse.success("测试邮件发送成功", null);
    }

    @PostMapping("/email/rules")
    public ApiResponse<EmailReminderRule> saveReminderRule(@Valid @RequestBody ReminderRuleRequest request) {
        EmailReminderRule rule = new EmailReminderRule();
        rule.setDaysBefore(request.getDaysBefore());
        rule.setSendHour(request.getSendHour());
        rule.setIsActive(1);
        return ApiResponse.success("提醒规则保存成功", emailService.saveReminderRule(rule));
    }

    @GetMapping("/email/rules")
    public ApiResponse<List<EmailReminderRule>> getReminderRules() {
        return ApiResponse.success(emailService.getActiveRules());
    }

    @DeleteMapping("/email/rules/{ruleId}")
    public ApiResponse<Void> deleteReminderRule(@PathVariable Long ruleId) {
        emailService.deleteReminderRule(ruleId);
        return ApiResponse.success("提醒规则删除成功", null);
    }
}

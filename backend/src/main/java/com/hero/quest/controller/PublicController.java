package com.hero.quest.controller;

import com.hero.quest.dto.*;
import com.hero.quest.entity.Task;
import com.hero.quest.service.TaskService;
import jakarta.validation.groups.Default;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final TaskService taskService;

    public PublicController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * 获取所有公开任务（任务榜）
     */
    @GetMapping("/tasks")
    public ApiResponse<List<TaskSummaryResponse>> getTasks() {
        return ApiResponse.success(taskService.getPublicTasks());
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/tasks/{taskId}")
    public ApiResponse<TaskDetailResponse> getTaskDetail(@PathVariable Long taskId) {
        return ApiResponse.success(taskService.getTaskDetail(taskId));
    }

    /**
     * 认领任务
     */
    @PostMapping("/tasks/{taskId}/claim")
    public ApiResponse<Void> claimTask(@PathVariable Long taskId,
                                       @Validated({ Default.class, TaskClaimRequest.PublicClaimGroup.class })
                                       @RequestBody TaskClaimRequest request) {
        taskService.claimTask(taskId, request);
        return ApiResponse.success("认领成功，请等待管理员审核", null);
    }

    /**
     * 修改认领申请（待审核状态下可修改）
     */
    @PutMapping("/tasks/{taskId}/claim")
    public ApiResponse<Void> updateTaskClaim(@PathVariable Long taskId,
                                             @Validated({ Default.class, TaskClaimRequest.PublicClaimGroup.class })
                                             @RequestBody TaskClaimRequest request) {
        taskService.updateTaskClaim(taskId, request);
        return ApiResponse.success("修改成功", null);
    }

    /**
     * 获取积分排行榜
     */
    @GetMapping("/leaderboard")
    public ApiResponse<List<LeaderboardEntry>> getLeaderboard(
            @RequestParam(defaultValue = "100") int limit) {
        return ApiResponse.success(taskService.getLeaderboard(limit));
    }
}

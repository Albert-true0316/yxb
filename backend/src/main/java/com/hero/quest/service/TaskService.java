package com.hero.quest.service;

import com.hero.quest.dto.*;
import com.hero.quest.entity.Task;
import java.util.List;

public interface TaskService {

    // 公开接口
    List<TaskSummaryResponse> getPublicTasks();

    TaskDetailResponse getTaskDetail(Long taskId);

    void claimTask(Long taskId, TaskClaimRequest request);

    void updateTaskClaim(Long taskId, TaskClaimRequest request);

    List<LeaderboardEntry> getLeaderboard(int limit);

    // 管理员接口
    Task createTask(TaskCreateRequest request);

    void approveTaskMember(Long taskId, Long memberId);

    void approveMemberOnly(Long taskId, Long memberId);

    void startTask(Long taskId);

    void rejectTaskMember(Long taskId, Long memberId);

    void removeMemberFromTask(Long taskId, Long memberId);

    void completeTask(Long taskId);

    void assignMemberDirectly(Long taskId, TaskClaimRequest request);

    List<Task> getAllTasks();

    List<Task> getTasksByStatus(Integer status);
}

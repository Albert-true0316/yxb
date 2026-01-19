package com.hero.quest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hero.quest.config.BusinessException;
import com.hero.quest.dto.*;
import com.hero.quest.entity.Task;
import com.hero.quest.entity.TaskMember;
import com.hero.quest.mapper.TaskMapper;
import com.hero.quest.mapper.TaskMemberMapper;
import com.hero.quest.service.AdminService;
import com.hero.quest.service.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskMapper taskMapper;
    private final TaskMemberMapper taskMemberMapper;
    private final AdminService adminService;

    public TaskServiceImpl(TaskMapper taskMapper, TaskMemberMapper taskMemberMapper,
                           AdminService adminService) {
        this.taskMapper = taskMapper;
        this.taskMemberMapper = taskMemberMapper;
        this.adminService = adminService;
    }

    @Override
    public List<TaskSummaryResponse> getPublicTasks() {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Task::getCreatedAt);
        List<Task> tasks = taskMapper.selectList(wrapper);

        List<TaskSummaryResponse> result = new ArrayList<>();
        for (Task task : tasks) {
            TaskSummaryResponse summary = new TaskSummaryResponse();
            summary.setId(task.getId());
            summary.setTitle(task.getTitle());
            summary.setDeadline(task.getDeadline());
            summary.setReward(task.getReward());
            summary.setStatus(task.getStatus());
            summary.setCreatedAt(task.getCreatedAt());

            LambdaQueryWrapper<TaskMember> memberWrapper = new LambdaQueryWrapper<>();
            memberWrapper.eq(TaskMember::getTaskId, task.getId())
                         .ne(TaskMember::getStatus, TaskMember.STATUS_QUIT);
            List<TaskMember> members = taskMemberMapper.selectList(memberWrapper);
            summary.setMemberNames(members.stream().map(TaskMember::getMemberName).toList());

            result.add(summary);
        }
        return result;
    }

    @Override
    public TaskDetailResponse getTaskDetail(Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        TaskDetailResponse response = new TaskDetailResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setContent(task.getContent());
        response.setDeadline(task.getDeadline());
        response.setReward(task.getReward());
        response.setStatus(task.getStatus());
        response.setStatusText(getStatusText(task.getStatus()));
        response.setCreatedAt(task.getCreatedAt());

        // 获取成员列表
        LambdaQueryWrapper<TaskMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(TaskMember::getTaskId, taskId);
        memberWrapper.ne(TaskMember::getStatus, TaskMember.STATUS_QUIT);
        List<TaskMember> members = taskMemberMapper.selectList(memberWrapper);

        List<TaskDetailResponse.MemberInfo> memberInfos = new ArrayList<>();
        for (TaskMember member : members) {
            TaskDetailResponse.MemberInfo info = new TaskDetailResponse.MemberInfo();
            info.setId(member.getId());
            info.setMemberName(member.getMemberName());
            info.setMemberEmail(member.getMemberEmail());
            info.setEarnedPoints(member.getEarnedPoints());
            info.setStatus(member.getStatus());
            info.setStatusText(getMemberStatusText(member.getStatus()));
            info.setCreatedAt(member.getCreatedAt());
            memberInfos.add(info);
        }
        response.setMembers(memberInfos);

        return response;
    }

    @Override
    @Transactional
    public void claimTask(Long taskId, TaskClaimRequest request) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        if (task.getStatus() != Task.STATUS_PENDING && task.getStatus() != Task.STATUS_REVIEWING) {
            throw new BusinessException("该任务当前状态不可认领");
        }

        validateExpectedPoints(request.getExpectedPoints(), task.getReward());
        validateTotalPoints(taskId, request.getExpectedPoints(), task.getReward(), null);

        // 检查是否已经认领过
        if (findActiveMember(taskId, request.getMemberEmail()) != null) {
            throw new BusinessException("您已认领过该任务");
        }

        // 更新任务信息（如果成员提供了修改）
        updateTaskFields(task, request.getTitle(), request.getContent(), request.getDeadline());

        // 创建成员记录
        TaskMember member = new TaskMember();
        member.setTaskId(taskId);
        member.setMemberName(request.getMemberName());
        member.setMemberEmail(request.getMemberEmail());
        Integer expectedPoints = request.getExpectedPoints();
        member.setEarnedPoints(expectedPoints != null ? expectedPoints : 0);
        member.setStatus(TaskMember.STATUS_PENDING);
        taskMemberMapper.insert(member);

        // 更新任务状态为待审核
        task.setStatus(Task.STATUS_REVIEWING);
        taskMapper.updateById(task);
    }

    @Override
    @Transactional
    public void updateTaskClaim(Long taskId, TaskClaimRequest request) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        // 查找当前用户的申请记录
        TaskMember member = findActiveMember(taskId, request.getMemberEmail());
        if (member == null) {
            throw new BusinessException("找不到您的申请记录");
        }

        if (member.getStatus() != TaskMember.STATUS_PENDING) {
            throw new BusinessException("申请已审核，无法修改");
        }

        validateExpectedPoints(request.getExpectedPoints(), task.getReward());
        validateTotalPoints(taskId, request.getExpectedPoints(), task.getReward(), request.getMemberEmail());

        // 更新成员信息
        if (StringUtils.hasText(request.getMemberName())) {
            member.setMemberName(request.getMemberName());
        }
        if (request.getExpectedPoints() != null) {
            member.setEarnedPoints(request.getExpectedPoints());
        }
        taskMemberMapper.updateById(member);

        // 任务进入"进行中"前，可以修改任务信息
        if (task.getStatus() < Task.STATUS_IN_PROGRESS) {
            if (updateTaskFields(task, request.getTitle(), request.getContent(), request.getDeadline())) {
                taskMapper.updateById(task);
            }
        }
    }

    @Override
    public List<LeaderboardEntry> getLeaderboard(int limit) {
        List<LeaderboardEntry> entries = taskMemberMapper.getLeaderboard(limit);
        // 添加排名
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setRank(i + 1);
        }
        return entries;
    }

    @Override
    public Task createTask(TaskCreateRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setContent(request.getContent());
        task.setDeadline(request.getDeadline());
        task.setReward(request.getReward());
        task.setStatus(Task.STATUS_PENDING);
        task.setCreatedBy(adminService.getCurrentAdmin().getId());
        taskMapper.insert(task);
        return task;
    }

    @Override
    @Transactional
    public void approveTaskMember(Long taskId, Long memberId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        TaskMember member = taskMemberMapper.selectById(memberId);
        if (member == null || !member.getTaskId().equals(taskId)) {
            throw new BusinessException("成员不存在");
        }

        if (member.getStatus() != TaskMember.STATUS_PENDING) {
            throw new BusinessException("成员状态不正确");
        }

        // 更新成员状态
        member.setStatus(TaskMember.STATUS_APPROVED);
        taskMemberMapper.updateById(member);

        // 更新任务状态为进行中
        task.setStatus(Task.STATUS_IN_PROGRESS);
        taskMapper.updateById(task);
    }

    @Override
    @Transactional
    public void approveMemberOnly(Long taskId, Long memberId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        TaskMember member = taskMemberMapper.selectById(memberId);
        if (member == null || !member.getTaskId().equals(taskId)) {
            throw new BusinessException("成员不存在");
        }

        if (member.getStatus() != TaskMember.STATUS_PENDING) {
            throw new BusinessException("成员状态不正确");
        }

        // 仅更新成员状态为已通过，不改变任务状态
        member.setStatus(TaskMember.STATUS_APPROVED);
        taskMemberMapper.updateById(member);
    }

    @Override
    @Transactional
    public void startTask(Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        if (task.getStatus() != Task.STATUS_REVIEWING) {
            throw new BusinessException("只有待审核状态的任务才能开始");
        }

        // 检查是否有已通过的成员
        LambdaQueryWrapper<TaskMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskMember::getTaskId, taskId);
        wrapper.eq(TaskMember::getStatus, TaskMember.STATUS_APPROVED);
        Long approvedCount = taskMemberMapper.selectCount(wrapper);

        if (approvedCount == 0) {
            throw new BusinessException("没有已通过的成员，无法开始任务");
        }

        // 更新任务状态为进行中
        task.setStatus(Task.STATUS_IN_PROGRESS);
        taskMapper.updateById(task);
    }

    @Override
    @Transactional
    public void rejectTaskMember(Long taskId, Long memberId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        TaskMember member = taskMemberMapper.selectById(memberId);
        if (member == null || !member.getTaskId().equals(taskId)) {
            throw new BusinessException("成员不存在");
        }

        // 删除成员记录
        taskMemberMapper.deleteById(memberId);

        // 检查是否还有其他成员
        LambdaQueryWrapper<TaskMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskMember::getTaskId, taskId);
        wrapper.ne(TaskMember::getStatus, TaskMember.STATUS_QUIT);
        Long count = taskMemberMapper.selectCount(wrapper);

        if (count == 0) {
            // 没有成员了，回到待认领状态
            task.setStatus(Task.STATUS_PENDING);
            taskMapper.updateById(task);
        }
    }

    @Override
    @Transactional
    public void removeMemberFromTask(Long taskId, Long memberId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        TaskMember member = taskMemberMapper.selectById(memberId);
        if (member == null || !member.getTaskId().equals(taskId)) {
            throw new BusinessException("成员不存在");
        }

        // 标记成员为已退出
        member.setStatus(TaskMember.STATUS_QUIT);
        taskMemberMapper.updateById(member);

        // 检查是否还有其他活跃成员
        LambdaQueryWrapper<TaskMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskMember::getTaskId, taskId);
        wrapper.eq(TaskMember::getStatus, TaskMember.STATUS_APPROVED);
        Long count = taskMemberMapper.selectCount(wrapper);

        if (count == 0) {
            // 没有活跃成员了，回到待认领状态
            task.setStatus(Task.STATUS_PENDING);
            taskMapper.updateById(task);
        }
    }

    @Override
    @Transactional
    public void completeTask(Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        if (task.getStatus() != Task.STATUS_IN_PROGRESS) {
            throw new BusinessException("只有进行中的任务才能标记完成");
        }

        // 积分由成员认领时自行分配，完成任务时不再重新分配
        // 直接更新任务状态为已完成
        task.setStatus(Task.STATUS_COMPLETED);
        taskMapper.updateById(task);
    }

    @Override
    @Transactional
    public void assignMemberDirectly(Long taskId, TaskClaimRequest request) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        Integer points = request.getExpectedPoints();
        if (points != null) {
            validateExpectedPoints(points, task.getReward());
            validateTotalPoints(taskId, points, task.getReward(), null);
        }

        // 更新任务信息
        if (StringUtils.hasText(request.getTitle())) {
            task.setTitle(request.getTitle());
        }
        if (StringUtils.hasText(request.getContent())) {
            task.setContent(request.getContent());
        }
        if (request.getDeadline() != null) {
            task.setDeadline(request.getDeadline());
        }

        // 创建成员记录（直接通过）
        TaskMember member = new TaskMember();
        member.setTaskId(taskId);
        member.setMemberName(request.getMemberName());
        member.setMemberEmail(request.getMemberEmail());
        member.setEarnedPoints(points != null ? points : 0);
        member.setStatus(TaskMember.STATUS_APPROVED);
        taskMemberMapper.insert(member);

        // 更新任务状态为进行中
        task.setStatus(Task.STATUS_IN_PROGRESS);
        taskMapper.updateById(task);
    }

    @Override
    public List<Task> getAllTasks() {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Task::getCreatedAt);
        return taskMapper.selectList(wrapper);
    }

    @Override
    public List<Task> getTasksByStatus(Integer status) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getStatus, status);
        wrapper.orderByDesc(Task::getCreatedAt);
        return taskMapper.selectList(wrapper);
    }

    private String getStatusText(Integer status) {
        return switch (status) {
            case Task.STATUS_PENDING -> "待认领";
            case Task.STATUS_REVIEWING -> "待审核";
            case Task.STATUS_IN_PROGRESS -> "进行中";
            case Task.STATUS_COMPLETED -> "已完成";
            default -> "未知";
        };
    }

    private String getMemberStatusText(Integer status) {
        return switch (status) {
            case TaskMember.STATUS_PENDING -> "待审核";
            case TaskMember.STATUS_APPROVED -> "已通过";
            case TaskMember.STATUS_QUIT -> "已退出";
            default -> "未知";
        };
    }

    private void validateExpectedPoints(Integer expectedPoints, Integer reward) {
        if (expectedPoints != null && expectedPoints > reward) {
            throw new BusinessException("期望积分不能超过任务悬赏");
        }
    }

    private void validateTotalPoints(Long taskId, Integer newPoints, Integer reward, String excludeEmail) {
        LambdaQueryWrapper<TaskMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskMember::getTaskId, taskId)
               .ne(TaskMember::getStatus, TaskMember.STATUS_QUIT);
        if (excludeEmail != null) {
            wrapper.ne(TaskMember::getMemberEmail, excludeEmail);
        }
        List<TaskMember> members = taskMemberMapper.selectList(wrapper);
        int existingTotal = members.stream().mapToInt(m -> m.getEarnedPoints() != null ? m.getEarnedPoints() : 0).sum();
        if (existingTotal + (newPoints != null ? newPoints : 0) > reward) {
            throw new BusinessException("积分总和不能超过任务悬赏 " + reward + "，当前已分配 " + existingTotal);
        }
    }

    private TaskMember findActiveMember(Long taskId, String email) {
        LambdaQueryWrapper<TaskMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskMember::getTaskId, taskId)
               .eq(TaskMember::getMemberEmail, email)
               .ne(TaskMember::getStatus, TaskMember.STATUS_QUIT);
        return taskMemberMapper.selectOne(wrapper);
    }

    private boolean updateTaskFields(Task task, String title, String content, LocalDate deadline) {
        boolean updated = false;
        if (StringUtils.hasText(title)) {
            task.setTitle(title);
            updated = true;
        }
        if (StringUtils.hasText(content)) {
            task.setContent(content);
            updated = true;
        }
        if (deadline != null) {
            task.setDeadline(deadline);
            updated = true;
        }
        return updated;
    }
}

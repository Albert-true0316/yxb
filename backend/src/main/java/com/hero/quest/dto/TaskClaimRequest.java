package com.hero.quest.dto;

import jakarta.validation.constraints.*;
import jakarta.validation.groups.Default;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TaskClaimRequest {

    public interface PublicClaimGroup {}

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名不能超过50字符")
    private String memberName;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String memberEmail;

    // 可选：成员可以修改的任务信息
    @Size(max = 200, message = "任务名称不能超过200字符")
    private String title;

    private String content;

    @NotNull(groups = PublicClaimGroup.class, message = "积分不能为空")
    @Min(value = 0, message = "积分不能为负数")
    private Integer expectedPoints;

    @NotNull(groups = PublicClaimGroup.class, message = "截止时间不能为空")
    @FutureOrPresent(groups = { Default.class, PublicClaimGroup.class }, message = "截止时间不能早于今天")
    private LocalDate deadline;
}

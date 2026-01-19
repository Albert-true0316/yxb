package com.hero.quest.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TaskCreateRequest {

    @NotBlank(message = "任务名称不能为空")
    @Size(max = 200, message = "任务名称不能超过200字符")
    private String title;

    private String content;

    @NotNull(message = "截止日期不能为空")
    @FutureOrPresent(message = "截止日期不能早于今天")
    private LocalDate deadline;

    @NotNull(message = "悬赏积分不能为空")
    @Min(value = 1, message = "悬赏积分必须大于0")
    private Integer reward;
}

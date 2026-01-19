package com.hero.quest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReminderRuleRequest {

    @NotNull(message = "提前天数不能为空")
    @Min(value = 0, message = "提前天数不能小于0")
    private Integer daysBefore;

    @NotNull(message = "发送小时不能为空")
    @Min(value = 0, message = "发送小时必须在0-23之间")
    @Max(value = 23, message = "发送小时必须在0-23之间")
    private Integer sendHour;
}

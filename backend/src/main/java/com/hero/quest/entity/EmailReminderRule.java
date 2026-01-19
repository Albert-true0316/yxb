package com.hero.quest.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("email_reminder_rule")
public class EmailReminderRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer daysBefore;

    private Integer sendHour;

    private Integer isActive;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

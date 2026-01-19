package com.hero.quest.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("task")
public class Task {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private LocalDate deadline;

    private Integer reward;

    /**
     * 状态：0-待认领，1-待审核，2-进行中，3-已完成
     */
    private Integer status;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 任务状态常量
    public static final int STATUS_PENDING = 0;      // 待认领
    public static final int STATUS_REVIEWING = 1;    // 待审核
    public static final int STATUS_IN_PROGRESS = 2;  // 进行中
    public static final int STATUS_COMPLETED = 3;    // 已完成
}

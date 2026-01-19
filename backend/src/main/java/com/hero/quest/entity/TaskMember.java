package com.hero.quest.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("task_member")
public class TaskMember {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private String memberName;

    private String memberEmail;

    private Integer earnedPoints;

    /**
     * 成员状态：0-待审核，1-已通过，2-已退出
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 成员状态常量
    public static final int STATUS_PENDING = 0;   // 待审核
    public static final int STATUS_APPROVED = 1;  // 已通过
    public static final int STATUS_QUIT = 2;      // 已退出
}

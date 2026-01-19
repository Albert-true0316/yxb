package com.hero.quest.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("email_log")
public class EmailLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private String memberEmail;

    /**
     * 邮件类型：0-截止提醒
     */
    private Integer emailType;

    /**
     * 发送状态：0-待发送，1-已发送，2-发送失败
     */
    private Integer status;

    private LocalDateTime sentAt;

    private String errorMsg;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // 邮件类型常量
    public static final int TYPE_DEADLINE_REMINDER = 0;

    // 发送状态常量
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_SENT = 1;
    public static final int STATUS_FAILED = 2;
}

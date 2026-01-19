-- Hero Quest Board Database Schema
-- 英雄任务榜数据库初始化脚本

CREATE DATABASE IF NOT EXISTS hero_quest DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE hero_quest;

-- 管理员表
CREATE TABLE IF NOT EXISTS admin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    email VARCHAR(100) COMMENT '管理员邮箱',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- 任务表
CREATE TABLE IF NOT EXISTS task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL COMMENT '任务名称',
    content TEXT COMMENT '任务内容（富文本）',
    deadline DATE NOT NULL COMMENT '截止日期',
    reward INT NOT NULL DEFAULT 0 COMMENT '悬赏积分',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待认领，1-待审核，2-进行中，3-已完成',
    created_by BIGINT NOT NULL COMMENT '创建者（管理员ID）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status),
    INDEX idx_deadline (deadline),
    INDEX idx_created_by (created_by),
    FOREIGN KEY (created_by) REFERENCES admin(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务表';

-- 任务成员关系表（多对多）
CREATE TABLE IF NOT EXISTS task_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL COMMENT '任务ID',
    member_name VARCHAR(50) NOT NULL COMMENT '成员姓名',
    member_email VARCHAR(100) NOT NULL COMMENT '成员邮箱',
    earned_points INT DEFAULT 0 COMMENT '获得积分（任务完成后由管理员分配）',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '成员状态：0-待审核，1-已通过，2-已退出',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '认领时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_task_id (task_id),
    INDEX idx_member_email (member_email),
    INDEX idx_status (status),
    FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务成员关系表';

-- 邮件发送记录表
CREATE TABLE IF NOT EXISTS email_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL COMMENT '任务ID',
    member_email VARCHAR(100) NOT NULL COMMENT '收件人邮箱',
    email_type TINYINT NOT NULL DEFAULT 0 COMMENT '邮件类型：0-截止提醒',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '发送状态：0-待发送，1-已发送，2-发送失败',
    sent_at DATETIME COMMENT '发送时间',
    error_msg VARCHAR(500) COMMENT '错误信息',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_task_id (task_id),
    INDEX idx_member_email (member_email),
    INDEX idx_status (status),
    FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮件发送记录表';

-- 邮件配置表
CREATE TABLE IF NOT EXISTS email_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    mail_username VARCHAR(100) NOT NULL COMMENT '邮箱账号',
    mail_password VARCHAR(255) NOT NULL COMMENT '邮箱密码/授权码',
    is_active TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮件配置表';

-- 邮件提醒规则表
CREATE TABLE IF NOT EXISTS email_reminder_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_type TINYINT NOT NULL COMMENT '规则类型：0-截止当天，1-提前N天，2-百分比',
    days_before INT COMMENT '提前天数（rule_type=1时使用）',
    percentage INT COMMENT '百分比（rule_type=2时使用，0-100）',
    send_hour INT NOT NULL DEFAULT 9 COMMENT '发送小时（0-23）',
    is_active TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮件提醒规则表';

-- 积分排行榜视图（按邮箱汇总已完成任务的积分）
CREATE OR REPLACE VIEW v_leaderboard AS
SELECT
    tm.member_email,
    tm.member_name,
    SUM(tm.earned_points) as total_points,
    COUNT(DISTINCT tm.task_id) as completed_tasks
FROM task_member tm
INNER JOIN task t ON tm.task_id = t.id
WHERE t.status = 3  -- 已完成
  AND tm.status = 1  -- 成员状态为已通过
  AND tm.earned_points > 0
GROUP BY tm.member_email
ORDER BY total_points DESC;

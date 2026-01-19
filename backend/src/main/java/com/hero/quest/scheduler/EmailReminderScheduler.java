package com.hero.quest.scheduler;

import com.hero.quest.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EmailReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(EmailReminderScheduler.class);

    private final EmailService emailService;

    @Value("${email.reminder.enabled:true}")
    private boolean reminderEnabled;

    public EmailReminderScheduler(EmailService emailService) {
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void sendDeadlineReminders() {
        if (!reminderEnabled) {
            log.info("邮件提醒功能已禁用");
            return;
        }

        log.info("开始执行截止日期邮件提醒任务");
        try {
            emailService.processDeadlineReminders();
            log.info("截止日期邮件提醒任务执行完成");
        } catch (Exception e) {
            log.error("截止日期邮件提醒任务执行失败", e);
        }
    }
}

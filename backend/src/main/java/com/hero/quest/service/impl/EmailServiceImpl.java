package com.hero.quest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hero.quest.config.BusinessException;
import com.hero.quest.entity.*;
import com.hero.quest.mapper.*;
import com.hero.quest.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Properties;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final TaskMapper taskMapper;
    private final TaskMemberMapper taskMemberMapper;
    private final EmailLogMapper emailLogMapper;
    private final EmailConfigMapper emailConfigMapper;
    private final EmailReminderRuleMapper reminderRuleMapper;

    @Value("${spring.mail.username}")
    private String defaultFromEmail;

    public EmailServiceImpl(JavaMailSender mailSender, TaskMapper taskMapper,
                            TaskMemberMapper taskMemberMapper, EmailLogMapper emailLogMapper,
                            EmailConfigMapper emailConfigMapper, EmailReminderRuleMapper reminderRuleMapper) {
        this.mailSender = mailSender;
        this.taskMapper = taskMapper;
        this.taskMemberMapper = taskMemberMapper;
        this.emailLogMapper = emailLogMapper;
        this.emailConfigMapper = emailConfigMapper;
        this.reminderRuleMapper = reminderRuleMapper;
    }

    private JavaMailSender getConfiguredMailSender() {
        EmailConfig config = getActiveEmailConfig();
        if (config == null) {
            return mailSender;
        }

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost("smtp.qq.com");
        sender.setPort(465);
        sender.setUsername(config.getMailUsername());
        sender.setPassword(config.getMailPassword());
        sender.setDefaultEncoding("UTF-8");

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.ssl.required", "true");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.timeout", "25000");
        props.put("mail.smtp.connectiontimeout", "25000");

        return sender;
    }

    private String getFromEmail() {
        EmailConfig config = getActiveEmailConfig();
        return config != null ? config.getMailUsername() : defaultFromEmail;
    }

    @Override
    public void sendDeadlineReminder(String toEmail, String memberName, String taskTitle, String deadline) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(getFromEmail());
            message.setTo(toEmail);
            message.setSubject("【英雄任务榜】任务截止提醒");
            message.setText(String.format("""
                亲爱的 %s：

                您好！

                您认领的任务「%s」将于今天（%s）截止，请及时完成并提交。

                如有任何问题，请联系管理员。

                祝您工作顺利！

                ——英雄任务榜
                """, memberName, taskTitle, deadline));

            getConfiguredMailSender().send(message);
            log.info("邮件发送成功: to={}, task={}", toEmail, taskTitle);
        } catch (Exception e) {
            log.error("邮件发送失败: to={}, error={}", toEmail, e.getMessage());
            throw e;
        }
    }

    @Override
    public void processDeadlineReminders() {
        List<EmailReminderRule> rules = getActiveRules();
        if (rules.isEmpty()) {
            log.info("没有启用的提醒规则");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        int currentHour = now.getHour();
        LocalDate today = now.toLocalDate();

        log.info("开始处理截止日期提醒，当前时间: {}", now);

        LambdaQueryWrapper<Task> taskWrapper = new LambdaQueryWrapper<>();
        taskWrapper.eq(Task::getStatus, Task.STATUS_IN_PROGRESS);
        List<Task> tasks = taskMapper.selectList(taskWrapper);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

        for (Task task : tasks) {
            for (EmailReminderRule rule : rules) {
                if (rule.getSendHour() != currentHour) {
                    continue;
                }

                boolean shouldSend = false;
                LocalDate targetDate = null;

                targetDate = task.getDeadline().minusDays(rule.getDaysBefore());

                if (targetDate.isBefore(task.getCreatedAt().toLocalDate())) {
                    log.info("规则提前天数过长，跳过: task={}, rule={}", task.getId(), rule.getId());
                    continue;
                }

                shouldSend = targetDate.equals(today);

                if (!shouldSend) {
                    continue;
                }

                LambdaQueryWrapper<TaskMember> memberWrapper = new LambdaQueryWrapper<>();
                memberWrapper.eq(TaskMember::getTaskId, task.getId());
                memberWrapper.eq(TaskMember::getStatus, TaskMember.STATUS_APPROVED);
                List<TaskMember> members = taskMemberMapper.selectList(memberWrapper);

                for (TaskMember member : members) {
                    LambdaQueryWrapper<EmailLog> logWrapper = new LambdaQueryWrapper<>();
                    logWrapper.eq(EmailLog::getTaskId, task.getId());
                    logWrapper.eq(EmailLog::getMemberEmail, member.getMemberEmail());
                    logWrapper.eq(EmailLog::getEmailType, EmailLog.TYPE_DEADLINE_REMINDER);
                    logWrapper.ge(EmailLog::getCreatedAt, today.atStartOfDay());

                    if (emailLogMapper.selectCount(logWrapper) > 0) {
                        log.info("今天已发送过邮件，跳过: task={}, email={}", task.getId(), member.getMemberEmail());
                        continue;
                    }

                    EmailLog emailLog = new EmailLog();
                    emailLog.setTaskId(task.getId());
                    emailLog.setMemberEmail(member.getMemberEmail());
                    emailLog.setEmailType(EmailLog.TYPE_DEADLINE_REMINDER);

                    try {
                        sendDeadlineReminder(
                                member.getMemberEmail(),
                                member.getMemberName(),
                                task.getTitle(),
                                task.getDeadline().format(formatter)
                        );
                        emailLog.setStatus(EmailLog.STATUS_SENT);
                        emailLog.setSentAt(LocalDateTime.now());
                    } catch (Exception e) {
                        emailLog.setStatus(EmailLog.STATUS_FAILED);
                        emailLog.setErrorMsg(e.getMessage());
                    }

                    emailLogMapper.insert(emailLog);
                }
            }
        }

        log.info("截止日期提醒处理完成");
    }

    @Override
    public EmailConfig saveEmailConfig(String mailUsername, String mailPassword) {
        LambdaQueryWrapper<EmailConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmailConfig::getIsActive, 1);
        emailConfigMapper.update(new EmailConfig() {{
            setIsActive(0);
        }}, wrapper);

        EmailConfig config = new EmailConfig();
        config.setMailUsername(mailUsername);
        config.setMailPassword(mailPassword);
        config.setIsActive(1);
        emailConfigMapper.insert(config);
        return config;
    }

    @Override
    public EmailConfig getActiveEmailConfig() {
        LambdaQueryWrapper<EmailConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmailConfig::getIsActive, 1);
        wrapper.last("LIMIT 1");
        return emailConfigMapper.selectOne(wrapper);
    }

    @Override
    public void testEmailSending(String toEmail) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(getFromEmail());
            message.setTo(toEmail);
            message.setSubject("【英雄任务榜】邮件测试");
            message.setText("这是一封测试邮件，如果您收到此邮件，说明邮件配置正确。");

            getConfiguredMailSender().send(message);
            log.info("测试邮件发送成功: to={}", toEmail);
        } catch (Exception e) {
            log.error("测试邮件发送失败: to={}, error={}", toEmail, e.getMessage());
            throw new BusinessException("邮件发送失败: " + e.getMessage());
        }
    }

    @Override
    public EmailReminderRule saveReminderRule(EmailReminderRule rule) {
        reminderRuleMapper.insert(rule);
        return rule;
    }

    @Override
    public List<EmailReminderRule> getActiveRules() {
        LambdaQueryWrapper<EmailReminderRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmailReminderRule::getIsActive, 1);
        return reminderRuleMapper.selectList(wrapper);
    }

    @Override
    public void deleteReminderRule(Long ruleId) {
        reminderRuleMapper.deleteById(ruleId);
    }
}

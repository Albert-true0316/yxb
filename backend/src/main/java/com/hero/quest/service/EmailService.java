package com.hero.quest.service;

import com.hero.quest.entity.EmailConfig;
import com.hero.quest.entity.EmailReminderRule;
import java.util.List;

public interface EmailService {

    void sendDeadlineReminder(String toEmail, String memberName, String taskTitle, String deadline);

    void processDeadlineReminders();

    EmailConfig saveEmailConfig(String mailUsername, String mailPassword);

    EmailConfig getActiveEmailConfig();

    void testEmailSending(String toEmail);

    EmailReminderRule saveReminderRule(EmailReminderRule rule);

    List<EmailReminderRule> getActiveRules();

    void deleteReminderRule(Long ruleId);
}

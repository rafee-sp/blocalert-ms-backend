package com.blocalert.notification.service;

import com.blocalert.notification.entity.EmailLog;
import java.util.List;

public interface EmailLogService {

    void saveLogs(List<EmailLog> emailLogList);

    void saveLog(EmailLog emailLog);
}

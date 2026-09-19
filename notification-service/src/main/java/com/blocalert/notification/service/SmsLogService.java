package com.blocalert.notification.service;

import com.blocalert.notification.entity.SmsLog;
import java.util.List;

public interface SmsLogService {

    void saveLogs(List<SmsLog> smsLogList);
}

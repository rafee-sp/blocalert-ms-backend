package com.blocalert.notification.service.impl;

import com.blocalert.notification.entity.SmsLog;
import com.blocalert.notification.repository.SmsLogRepository;
import com.blocalert.notification.service.SmsLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsLogServiceImpl implements SmsLogService {

    private final SmsLogRepository smsLogRepository;

    @Async
    @Override
    public void saveLogs(List<SmsLog> smsLogList) {

        log.info("saveLogs called for {} logs", smsLogList.size()); // TODO : Partition for large data set
        smsLogRepository.saveAll(smsLogList);
    }
}

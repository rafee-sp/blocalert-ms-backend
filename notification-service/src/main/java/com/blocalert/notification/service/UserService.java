package com.blocalert.notification.service;

import com.blocalert.dto.UserContactInfo;
import java.util.Map;
import java.util.Set;

public interface UserService {

    Map<Long, UserContactInfo> fetchContactInfo(Set<Long> userIds);
}

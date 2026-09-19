package com.blocalert.alert.service.impl;

import com.blocalert.dto.AlertDeliveryStatus;
import com.blocalert.dto.AssistantAlertRequest;
import com.blocalert.enums.AlertCondition;
import com.blocalert.enums.UserRole;
import com.blocalert.alert.dto.internal.AlertCache;
import com.blocalert.alert.dto.request.AlertRequest;
import com.blocalert.alert.dto.response.ActiveAlertResponse;
import com.blocalert.alert.dto.response.PastAlertResponse;
import com.blocalert.alert.entity.Alert;
import com.blocalert.alert.exception.AlertLimitExceedException;
import com.blocalert.alert.exception.DuplicateAlertException;
import com.blocalert.alert.exception.ResourceNotFoundException;
import com.blocalert.alert.repository.AlertCacheRepository;
import com.blocalert.alert.repository.AlertRepository;
import com.blocalert.alert.service.AlertService;
import com.blocalert.alert.service.CryptoService;
import com.blocalert.alert.service.UserService;
import com.blocalert.dto.CryptoPrice;
import com.blocalert.dto.TriggeredAlert;
import com.blocalert.event.AlertNotificationEvent;
import com.blocalert.event.CryptoPriceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertServiceImpl implements AlertService {


    private final AlertCacheRepository alertCacheRepository;
    private final UserService userService;
    private final AuthService authService;
    private final AlertRepository alertRepository;
    private final CryptoService cryptoService;
    private final ApplicationEventPublisher alertPublisher;

    private static final int FREE_ALERT_LIMIT = 5;

    @Override
    public void evaluateAndPublishAlerts(CryptoPriceEvent event) {

        log.info("evaluateAlerts called");

        List<CryptoPrice> cryptoPrices = event.cryptoPrices();

        Set<String> cryptoIds = cryptoPrices.stream()
                .map(CryptoPrice::id)
                .collect(Collectors.toSet());

        Map<String, List<AlertCache>> alertsByCrypto = alertCacheRepository.findByCryptoIds(cryptoIds);

        List<TriggeredAlert> triggerAlerts = cryptoPrices.stream()
                .flatMap(crypto -> alertsByCrypto
                        .getOrDefault(crypto.id(), Collections.emptyList())
                        .stream()
                        .filter(alert -> shouldTriggerAlert(crypto, alert))
                        .map(alert -> mapToTriggeredAlert(alert, crypto))
                )
                .toList();

        if(triggerAlerts.isEmpty()) {
            log.info("No valid alerts present for this event");
            return;
        }

        alertPublisher.publishEvent(new AlertNotificationEvent(triggerAlerts));
    }

    @Transactional
    @Override
    public void addAlert(AlertRequest request) {

        Long userId = getUserId();

        log.info("addAlert called fro {}", userId);

        checkUserLimit(userId);

        checkAndThrowDuplicateAlert(userId, request);

        Alert alert = new Alert();
        updateAlertFields(alert, request, userId);

        alert = alertRepository.save(alert);

        alertCacheRepository.save(AlertCache.from(alert, userId));
    }

    @Transactional
    @Override
    public void updateAlert(Long alertId, AlertRequest request){

        Long userId = getUserId();

        log.info("updateAlert called for alertId {} userId {}", alertId, userId);

        Alert alert = alertRepository.findByIdAndUserId(alertId, userId)
                            .orElseThrow(() -> new ResourceNotFoundException("Alert not found " + alertId));

        checkAndThrowDuplicateAlert(userId, request);

        updateAlertFields(alert, request, userId);

        alert = alertRepository.save(alert);

        alertCacheRepository.save(AlertCache.from(alert, userId));
    }

    @Transactional
    @Override
    public void deleteAlert(Long alertId){

        log.info("deleteAlert called for alert {}", alertId);

        Long userId = getUserId();

        Alert alert = alertRepository.findByIdAndUserId(alertId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found " + alertId));

        alertRepository.delete(alert);

        alertCacheRepository.delete(alertId, alert.getCryptoId());

    }

    @Transactional(readOnly = true)
    @Override
    public Page<ActiveAlertResponse> getActiveAlerts(String cryptoId, Pageable pageable) {

        log.info("getActiveAlerts called for cryptoId {}", cryptoId);

        Long userId = getUserId();

        Page<ActiveAlertResponse> alerts = (!StringUtils.hasText(cryptoId)) ? getPaginatedActiveAlerts(userId, pageable) : getPaginatedActiveAlertsOfCrypto(userId, cryptoId, pageable);

        log.info("alerts size : {}", alerts.getContent().size());

        return alerts;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<PastAlertResponse> getPastAlerts(String cryptoId, Pageable pageable) {

        log.info("getPastAlerts called for cryptoId {}", cryptoId);

        Long userId = getUserId();

        Page<PastAlertResponse> alerts = (!StringUtils.hasText(cryptoId)) ? getPaginatedPastAlerts(userId, pageable) : getPaginatedPastAlertsOfCrypto(userId, cryptoId, pageable);

        log.info("past alerts size : {}", alerts.getContent().size());

        return alerts;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Alert> getAlertsByIds(List<Long> alertIds) {

        if (alertIds == null || alertIds.isEmpty())
            throw new IllegalArgumentException("Alert Ids are not valid");

        return alertRepository.findAllById(alertIds);
    }

    @Async
    @Transactional
    @Override
    public void setAlertAsTriggered(List<AlertDeliveryStatus> deliveryResults) {

        log.info("setAlertAsTriggered called for deliveries : {}", deliveryResults.size());

        if (deliveryResults.isEmpty()) return;

        List<Long> alertIds = deliveryResults.stream()
                .map(AlertDeliveryStatus::alertId)
                .distinct()
                .toList();

        alertRepository.updateAlertAsTriggered(alertIds, LocalDateTime.now());
    }

    @Async
    @Override
    public void cleanupTriggeredAlerts(List<TriggeredAlert> alerts) {

        log.info("cleanupTriggeredAlerts called for : {}", alerts.size());

        if (alerts.isEmpty()) return;

        try {
            Map<String, List<Long>> alertsByCrypto = alerts.stream()
                    .collect(Collectors.groupingBy(
                            TriggeredAlert::cryptoId,
                            Collectors.mapping(TriggeredAlert::alertId, Collectors.toList())
                    ));

            alertsByCrypto.forEach(alertCacheRepository::deleteAll);
            log.info("Completed triggered alert cleanup for {} alerts across {} cryptos", alerts.size(), alertsByCrypto.size());

        } catch (Exception e) {
            log.error("Error during triggered alert cleanup: {}", e.getMessage(), e);
        }
    }

    @Override
    public Long createAlertFromAssistant(AssistantAlertRequest request) {

        log.info("createAlertFromAssistant called for {} for {}", request.userId(), request.cryptoId());

        Alert alert = new Alert();
        alert.setUserId(request.userId());
        alert.setCryptoId(request.cryptoId());
        alert.setCondition(request.condition());
        alert.setThresholdValue(request.thresholdValue());
        alert.setNotificationWebsocket(true); // default true
        alert.setNotificationEmail(request.notificationEmail());
        alert.setNotificationSms(request.notificationSms());

        alert = alertRepository.save(alert);

        alertCacheRepository.save(AlertCache.from(alert, request.userId()));

        return alert.getId();
    }

    private boolean shouldTriggerAlert(CryptoPrice crypto, AlertCache alert) {

        BigDecimal thresholdPrice = alert.getThresholdValue();
        BigDecimal currentPrice = crypto.current_price();

        int decimals = thresholdPrice.scale();
        BigDecimal roundedCurrent = currentPrice.setScale(decimals, RoundingMode.DOWN);
        BigDecimal roundedThreshold = thresholdPrice.setScale(decimals, RoundingMode.DOWN);

        return switch (alert.getAlertCondition()) {

            case AlertCondition.PRICE_ABOVE -> roundedCurrent.compareTo(roundedThreshold) > 0;
            case AlertCondition.PRICE_BELOW -> roundedCurrent.compareTo(roundedThreshold) < 0;
            case AlertCondition.PRICE_EQUALS -> roundedCurrent.compareTo(roundedThreshold) == 0;
        };
    }

    private void checkUserLimit(Long userId) {

        if (authService.hasRole(UserRole.ROLE_PREMIUM_USER.name())) return;

        int activeAlerts = alertRepository.countActiveAlertsByUserId(userId);

        if (activeAlerts >= FREE_ALERT_LIMIT)
            throw new AlertLimitExceedException("Alert Limit exceeded");
    }

    private Long getUserId(){
        return userService.getCurrentUserId();
    }

    private void checkAndThrowDuplicateAlert(Long userId, AlertRequest request) {

        // Check channel too
        if (alertRepository.existsByAlert(
                userId, request.getCryptoId(), request.getCondition(), request.getThresholdValue()))
            throw new DuplicateAlertException("Alert already exists for this condition");
    }

    private void updateAlertFields(Alert alert, AlertRequest request, Long userId) {

        alert.setUserId(userId);
        alert.setCryptoId(request.getCryptoId());
        alert.setCondition(request.getCondition());
        alert.setThresholdValue(request.getThresholdValue());
        alert.setNotificationWebsocket(request.getNotificationWebsocket());
        alert.setNotificationEmail(request.getNotificationEmail());
        alert.setNotificationSms(request.getNotificationSms());
    }

    private Page<ActiveAlertResponse> getPaginatedActiveAlertsOfCrypto(Long userId, String cryptoId, Pageable pageable) {

        log.info("getPaginatedActiveAlertsOfCrypto called for {}, userId {}", cryptoId, userId);

        Page<Alert> alertsPage = alertRepository.getActiveAlerts(userId, cryptoId, pageable);

        if (alertsPage.getContent().isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        return mapToActiveAlertResponse(alertsPage.getContent(), Set.of(cryptoId), pageable, alertsPage.getTotalElements());

    }

    private Page<ActiveAlertResponse> getPaginatedActiveAlerts(Long userId, Pageable pageable) {

        log.info("getPaginatedActiveAlerts called for userId {}", userId);

        Page<Alert> alertsPage = alertRepository.getActiveAlerts(userId, null, pageable);

        if (alertsPage.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        List<Alert> alertList = alertsPage.getContent();

        Set<String> cryptoIds = alertList.stream().map(Alert::getCryptoId).collect(Collectors.toSet());

        return mapToActiveAlertResponse(alertList, cryptoIds, pageable, alertsPage.getTotalElements());

    }

    private Page<ActiveAlertResponse> mapToActiveAlertResponse(List<Alert> activeAlertsList, Set<String> cryptoIds, Pageable pageable, Long totalElements) {

        Map<String, CryptoPrice> cryptoDataMap = getAndValidateCryptoData(cryptoIds);

        List<ActiveAlertResponse> activeAlertsResponse = activeAlertsList.stream().map(alert -> {
                    CryptoPrice crypto = cryptoDataMap.get(alert.getCryptoId());

                    return new ActiveAlertResponse(alert, crypto);
                })
                .toList();

        return new PageImpl<>(activeAlertsResponse, pageable, totalElements);

    }

    private Map<String, CryptoPrice> getAndValidateCryptoData(Set<String> cryptoIds) {

        Map<String, CryptoPrice> cryptoDataMap = cryptoService.getCryptoPriceDetails(cryptoIds);

        List<String> missingIds = cryptoIds.stream()
                .filter(id -> !cryptoDataMap.containsKey(id))
                .toList();

        if (!missingIds.isEmpty()) {
            log.error("Crypto Data not found for Id : {}", missingIds);
            throw new ResourceNotFoundException("Crypto data not found for " + missingIds.size() + " ID(s): " + String.join(", ", missingIds));
        }

        return cryptoDataMap;
    }

    private Page<PastAlertResponse> getPaginatedPastAlertsOfCrypto(Long userId, String cryptoId, Pageable pageable) {

        log.info("getPaginatedPastAlertsOfCrypto called for {}, userId {}", cryptoId, userId);

        Page<Long> alertIdsPage = alertRepository.getPastAlertIds(userId, cryptoId, pageable);

        if (alertIdsPage.getContent().isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        List<Alert> alertList = alertRepository.getPastAlertsByIds(alertIdsPage.getContent());

        return mapToPastAlertResponse(alertList, Set.of(cryptoId), pageable, alertIdsPage.getTotalElements());

    }

    private Page<PastAlertResponse> getPaginatedPastAlerts(Long userId, Pageable pageable) {

        log.info("getPaginatedPastAlerts called for userId {}", userId);

        Page<Long> alertIdsPage = alertRepository.getPastAlertIds(userId, null, pageable);

        if (alertIdsPage.getContent().isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        List<Alert> alertList = alertRepository.getPastAlertsByIds(alertIdsPage.getContent());

        Set<String> cryptoIds = alertList.stream().map(Alert::getCryptoId).collect(Collectors.toSet());

        return mapToPastAlertResponse(alertList, cryptoIds, pageable, alertIdsPage.getTotalElements());

    }


    private Page<PastAlertResponse> mapToPastAlertResponse(List<Alert> pastAlertsList, Set<String> cryptoIds, Pageable pageable, Long totalElements) {

        Map<String, CryptoPrice> cryptoDataMap = getAndValidateCryptoData(cryptoIds);

        List<PastAlertResponse> pastAlertsResponse = pastAlertsList.stream().map(alert -> {
                    CryptoPrice crypto = cryptoDataMap.get(alert.getCryptoId());

                    return PastAlertResponse.from(alert, crypto);
                })
                .toList();

        return new PageImpl<>(pastAlertsResponse, pageable, totalElements);

    }

    public TriggeredAlert mapToTriggeredAlert(AlertCache alert, CryptoPrice cryptoPrice) {

        return new TriggeredAlert(
                alert.getAlertId(),
                alert.getUserId(),
                cryptoPrice.id(),
                cryptoPrice.name(),
                cryptoPrice.image(),
                alert.getThresholdValue(),
                alert.getAlertCondition(),
                cryptoPrice.current_price(),
                alert.isNotificationWebsocket(),
                alert.isNotificationSms(),
                alert.isNotificationEmail()
        );
    }

}

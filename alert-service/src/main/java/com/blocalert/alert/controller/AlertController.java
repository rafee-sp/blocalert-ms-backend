package com.blocalert.alert.controller;

import com.blocalert.alert.dto.request.AlertRequest;
import com.blocalert.alert.dto.response.ActiveAlertResponse;
import com.blocalert.alert.dto.response.PastAlertResponse;
import com.blocalert.alert.service.AlertService;
import com.blocalert.dto.PaginationInfo;
import com.blocalert.dto.AssistantAlertRequest;
import com.blocalert.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Slf4j
public class AlertController {

    private final AlertService alertService;

    @PostMapping
    public ResponseEntity<Void> addAlert(@RequestBody @Valid AlertRequest request){

        log.debug("addAlert called");

        alertService.addAlert(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateAlert(@PathVariable Long id, @RequestBody @Valid AlertRequest request){

        log.debug("updateAlert called");

        alertService.updateAlert(id, request);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse> getActiveAlerts(@RequestParam(required = false) String cryptoId,
                                                       Pageable pageable) {

        log.debug("getActiveAlerts called");

        Page<ActiveAlertResponse> activeAlertsPage = alertService.getActiveAlerts(cryptoId, pageable);

        List<ActiveAlertResponse> activeAlertsList = activeAlertsPage.getContent();

        if (activeAlertsList.isEmpty()) {
            return ResponseEntity.ok().body(new ApiResponse("No Active alerts found", activeAlertsList));
        }

        return ResponseEntity.ok().body(
                new ApiResponse("Active alerts fetched", activeAlertsList, mapToPaginationDto(activeAlertsPage)));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse> getPastAlerts(@RequestParam(required = false) String cryptoId, Pageable pageable) {

        log.debug("getPastAlerts called");

        Page<PastAlertResponse> pastAlertsPage = alertService.getPastAlerts(cryptoId, pageable);

        List<PastAlertResponse> pastAlertsList = pastAlertsPage.getContent();

        if (pastAlertsList.isEmpty()) {
            return ResponseEntity.ok().body(new ApiResponse("No Past alerts found", pastAlertsList));
        }

        return ResponseEntity.ok().body(
                new ApiResponse("Past alerts fetched", pastAlertsList, mapToPaginationDto(pastAlertsPage)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteAlert(@PathVariable Long id) throws AccessDeniedException {

        log.debug("deleteAlert called");

        alertService.deleteAlert(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assistant")
    Long createAssistantAlert(@RequestBody AssistantAlertRequest request){
        return alertService.createAlertFromAssistant(request);
    }

    private PaginationInfo mapToPaginationDto(Page<?> page) {
        return new PaginationInfo(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }

}

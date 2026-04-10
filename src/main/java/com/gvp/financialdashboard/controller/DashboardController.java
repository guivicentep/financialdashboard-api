package com.gvp.financialdashboard.controller;

import com.gvp.financialdashboard.domain.dto.DashboardFilterRequest;
import com.gvp.financialdashboard.domain.dto.DashboardSummaryResponse;
import com.gvp.financialdashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary(
            @RequestHeader("X-User-Id") UUID userId,
            @ModelAttribute DashboardFilterRequest filter
    ) {
        return ResponseEntity.ok(dashboardService.getSummary(filter, userId));
    }
}
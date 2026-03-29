package com.trackseries.controller;

import com.trackseries.dto.MonthlyStatsDto;
import com.trackseries.service.StatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {
    private static final Logger log = LoggerFactory.getLogger(StatsController.class);

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/monthly")
    public ResponseEntity<MonthlyStatsDto> getMonthlyStats(
            Authentication authentication,
            @RequestParam(required = false) Integer year) {
        String username = authentication.getName();
        log.debug("/api/stats/monthly called, username='{}', year={}", username, year);
        return ResponseEntity.ok(statsService.getMonthlyStatsForUsername(username, year));
    }
}

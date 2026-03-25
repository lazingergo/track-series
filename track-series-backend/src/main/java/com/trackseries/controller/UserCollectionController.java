package com.trackseries.controller;

import com.trackseries.dto.UpNextDto;
import com.trackseries.entity.TrackedSeries;
import com.trackseries.enums.WatchStatus;
import com.trackseries.service.TrackedSeriesService;
import com.trackseries.service.UpNextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/collection")
public class UserCollectionController {
    private static final Logger log = LoggerFactory.getLogger(UserCollectionController.class);

    private final TrackedSeriesService trackedSeriesService;
    private final UpNextService upNextService;

    public UserCollectionController(TrackedSeriesService trackedSeriesService, UpNextService upNextService) {
        this.trackedSeriesService = trackedSeriesService;
        this.upNextService = upNextService;
    }

    // URL: GET /api/collection/up-next
    @GetMapping("/up-next")
    public ResponseEntity<UpNextDto> getUpNext(Authentication authentication) {
        String username = authentication.getName();
        log.debug("/api/collection/up-next called, username='{}'", username);
        UpNextDto response = upNextService.getUpNextForUsername(username);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/set-status/{seriesId}/{status}")
    public ResponseEntity<TrackedSeries> setStatusSeries(
            @PathVariable Long seriesId,
            @PathVariable WatchStatus status,
            Authentication authentication) {
        String username = authentication.getName();
        log.debug("/api/collection/set-status/{}/{} called, username='{}'", seriesId, status, username);

        try {
            TrackedSeries result = trackedSeriesService.updateSeriesStatusForUsername(username, seriesId, status);
            return ResponseEntity.ok(result);
        } catch (RuntimeException ex) {
            try {
                trackedSeriesService.addSeriesToCollectionForUsername(username, seriesId, status);
                TrackedSeries result = trackedSeriesService.updateSeriesStatusForUsername(username, seriesId, status);
                return ResponseEntity.ok(result);
            } catch (RuntimeException e) {
                return ResponseEntity.notFound().build();
            }
        }
    }

    @DeleteMapping("/{seriesId}")
    public ResponseEntity<Void> removeSeriesFromCollection(
            @PathVariable Long seriesId,
            Authentication authentication) {
        String username = authentication.getName();
        log.debug("/api/collection/{} DELETE called, username='{}'", seriesId, username);

        try {
            trackedSeriesService.removeSeriesFromCollectionForUsername(username, seriesId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @PostMapping("/{seriesId}/rate")
    public ResponseEntity<TrackedSeries> rateSeries(
            @PathVariable Long seriesId,
            @RequestParam Integer value,
            Authentication authentication) {
        String username = authentication.getName();
        log.debug("/api/collection/{}/rate called, username='{}', value={}", seriesId, username, value);

        try {
            TrackedSeries result = trackedSeriesService.updateSeriesRatingForUsername(username, seriesId, value);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            // Return 400 Bad Request if rating is not between 1 and 10
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            // Return 404 Not Found if the series is not in the user's collection
            return ResponseEntity.notFound().build();
        }
    }

}
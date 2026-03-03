package com.trackseries.controller;

import com.trackseries.dto.UpNextDto;
import com.trackseries.entity.TrackedSeries;
import com.trackseries.entity.User;
import com.trackseries.enums.WatchStatus;
import com.trackseries.service.TrackedSeriesService;
import com.trackseries.service.UpNextService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/collection")
public class UserCollectionController {

    private final TrackedSeriesService trackedSeriesService;
    private final UpNextService upNextService;

    public UserCollectionController(TrackedSeriesService trackedSeriesService, UpNextService upNextService) {
        this.trackedSeriesService = trackedSeriesService;
        this.upNextService = upNextService;
    }

    // URL: POST /api/collection/169?status=WATCHING
    @PostMapping("/{seriesId}")
    public ResponseEntity<TrackedSeries> addToCollection(
            @PathVariable Long seriesId,
            @RequestParam(defaultValue = "PLAN_TO_WATCH") WatchStatus status,
            @AuthenticationPrincipal User user) {

        TrackedSeries result = trackedSeriesService.addOrUpdateCollection(user.getId(), seriesId, status);
        return ResponseEntity.ok(result);
    }

    // URL: GET /api/collection/up-next
    @GetMapping("/up-next")
    public ResponseEntity<UpNextDto> getUpNext(@AuthenticationPrincipal User user) {
        UpNextDto response = upNextService.getUpNextForUser(user.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{seriesId}/rate")
    public ResponseEntity<TrackedSeries> rateSeries(
            @PathVariable Long seriesId,
            @RequestParam Integer value,
            @AuthenticationPrincipal User user) {

        try {
            TrackedSeries result = trackedSeriesService.updateRating(user.getId(), seriesId, value);
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
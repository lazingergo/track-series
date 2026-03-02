package com.trackseries.controller;

import com.trackseries.entity.TrackedSeries;
import com.trackseries.enums.WatchStatus;
import com.trackseries.service.TrackedSeriesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}/collection")
public class UserCollectionController {

    private final TrackedSeriesService trackedSeriesService;

    public UserCollectionController(TrackedSeriesService trackedSeriesService) {
        this.trackedSeriesService = trackedSeriesService;
    }

    @PostMapping("/{seriesId}")
    public ResponseEntity<TrackedSeries> addToCollection(
            @PathVariable Long userId,
            @PathVariable Long seriesId,
            @RequestParam(defaultValue = "PLAN_TO_WATCH") WatchStatus status){

        TrackedSeries result = trackedSeriesService.addOrUpdateCollection(userId, seriesId, status);
        return ResponseEntity.ok(result);
    }
}

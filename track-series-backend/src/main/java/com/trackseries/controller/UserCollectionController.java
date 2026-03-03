package com.trackseries.controller;

import com.trackseries.dto.UpNextDto;
import com.trackseries.entity.TrackedSeries;
import com.trackseries.enums.WatchStatus;
import com.trackseries.service.TrackedSeriesService;
import com.trackseries.service.UpNextService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}/collection")
public class UserCollectionController {

    private final TrackedSeriesService trackedSeriesService;
    private final UpNextService upNextService;

    public UserCollectionController(TrackedSeriesService trackedSeriesService, UpNextService upNextService) {
        this.trackedSeriesService = trackedSeriesService;
        this.upNextService = upNextService;
    }

    @PostMapping("/{seriesId}")
    public ResponseEntity<TrackedSeries> addToCollection(
            @PathVariable Long userId,
            @PathVariable Long seriesId,
            @RequestParam(defaultValue = "PLAN_TO_WATCH") WatchStatus status){

        TrackedSeries result = trackedSeriesService.addOrUpdateCollection(userId, seriesId, status);
        return ResponseEntity.ok(result);
    }

    // GET /api/users/1/collection/up-next
    @GetMapping("/up-next")
    public ResponseEntity<UpNextDto> getUpNext(@PathVariable Long userId) {
        UpNextDto response = upNextService.getUpNextForUser(userId);
        return ResponseEntity.ok(response);
    }

}

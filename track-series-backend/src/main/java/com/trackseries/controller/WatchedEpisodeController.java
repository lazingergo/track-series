package com.trackseries.controller;

import com.trackseries.service.WatchedEpisodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}/episodes/{episodeId}")
public class WatchedEpisodeController {
    private final WatchedEpisodeService watchedEpisodeService;

    public WatchedEpisodeController(WatchedEpisodeService watchedEpisodeService) {
        this.watchedEpisodeService = watchedEpisodeService;
    }


    @PostMapping("/watch")
    public ResponseEntity<Void> watchEpisode(
            @PathVariable Long userId,
            @PathVariable Long episodeId,
            @RequestParam(defaultValue = "false") boolean includePrevious) {

        watchedEpisodeService.markEpisodeAsWatched(userId, episodeId, includePrevious);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/watch")
    public ResponseEntity<Void> unwatchEpisode(
            @PathVariable Long userId,
            @PathVariable Long episodeId) {

        watchedEpisodeService.unmarkEpisodeAsWatched(userId, episodeId);
        return ResponseEntity.ok().build();
    }
}


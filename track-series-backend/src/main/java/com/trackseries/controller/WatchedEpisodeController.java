package com.trackseries.controller;

import com.trackseries.service.WatchedEpisodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/episodes/{episodeId}")
public class WatchedEpisodeController {
    private static final Logger log = LoggerFactory.getLogger(WatchedEpisodeController.class);

    private final WatchedEpisodeService watchedEpisodeService;

    public WatchedEpisodeController(WatchedEpisodeService watchedEpisodeService) {
        this.watchedEpisodeService = watchedEpisodeService;
    }

    // POST: Mark as watched.
    // Optional params: ?includePrevious=true & ?watchedAt=2026-03-03T20:00:00
    @PostMapping("/watch")
    public ResponseEntity<Void> watchEpisode(
            @PathVariable Long episodeId,
            @RequestParam(defaultValue = "false") boolean includePrevious,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime watchedAt,
            Authentication authentication) {

        log.debug("/api/episodes/{}/watch POST called, username='{}', includePrevious={}, watchedAt={}",
            episodeId,
            authentication.getName(),
            includePrevious,
            watchedAt);
        watchedEpisodeService.markEpisodeAsWatchedByUsername(authentication.getName(), episodeId, includePrevious, watchedAt);
        return ResponseEntity.ok().build();
    }

    // DELETE: Unmark as watched
    @DeleteMapping("/watch")
    public ResponseEntity<Void> unwatchEpisode(
            @PathVariable Long episodeId,
            Authentication authentication) {

        log.debug("/api/episodes/{}/watch DELETE called, username='{}'", episodeId, authentication.getName());
        watchedEpisodeService.unmarkEpisodeAsWatchedByUsername(authentication.getName(), episodeId);
        return ResponseEntity.ok().build();
    }


}
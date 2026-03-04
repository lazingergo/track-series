package com.trackseries.controller;

import com.trackseries.entity.TrackedSeries;
import com.trackseries.entity.User;
import com.trackseries.service.WatchedEpisodeService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.sound.midi.Track;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/episodes/{episodeId}")
public class WatchedEpisodeController {

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

        watchedEpisodeService.markEpisodeAsWatchedByUsername(authentication.getName(), episodeId, includePrevious, watchedAt);
        return ResponseEntity.ok().build();
    }

    // DELETE: Unmark as watched
    @DeleteMapping("/watch")
    public ResponseEntity<Void> unwatchEpisode(
            @PathVariable Long episodeId,
            Authentication authentication) {

        watchedEpisodeService.unmarkEpisodeAsWatchedByUsername(authentication.getName(), episodeId);
        return ResponseEntity.ok().build();
    }


}
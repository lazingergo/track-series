package com.trackseries.controller;

import com.trackseries.dto.SeriesDetailsDto;
import com.trackseries.dto.SeriesSearchResultDto;
import com.trackseries.entity.Series;
import com.trackseries.entity.User;
import com.trackseries.enums.WatchStatus;
import com.trackseries.repository.SeriesRepository;
import com.trackseries.service.SeriesDetailsService;
import com.trackseries.service.TrackedSeriesService;
import com.trackseries.service.TvMazeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/series")
public class SeriesController {

    private final TvMazeService tvMazeService;
    private final SeriesRepository seriesRepository;
    private final SeriesDetailsService seriesDetailsService;
    TrackedSeriesService trackedSeriesService;

    public SeriesController(TvMazeService tvMazeService,
                            SeriesRepository seriesRepository,
                            SeriesDetailsService seriesDetailsService,
                            TrackedSeriesService trackedSeriesService) {
        this.tvMazeService = tvMazeService;
        this.seriesRepository = seriesRepository;
        this.seriesDetailsService = seriesDetailsService;
        this.trackedSeriesService = trackedSeriesService;
    }

    // Import a series from tvMaze
    @PostMapping("/import/{tvMazeId}")
    public ResponseEntity<Series> importSeries(
            @PathVariable Long tvMazeId,
            Authentication authentication
    ) {
        Series savedSeries = tvMazeService.fetchAndSaveSeries(tvMazeId);
        trackedSeriesService.addOrUpdateCollectionByUsername(authentication.getName(), savedSeries.getId(), WatchStatus.WATCHING);
        return ResponseEntity.ok(savedSeries);
    }

    // Get all series
    @GetMapping
    public ResponseEntity<List<Series>> getAllSeries() {
        List<Series> seriesList = seriesRepository.findAll();
        return ResponseEntity.ok(seriesList);
    }

    // Get series by ID
    @GetMapping("/{id}")
    public ResponseEntity<Series> getSeriesById(@PathVariable Long id) {
        return seriesRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build()); // 404 if the id doesn't exist
    }

    // URL: GET /api/series/169/details
    @GetMapping("/{seriesId}/details")
    public ResponseEntity<SeriesDetailsDto> getSeriesDetailsForUser(
            @PathVariable Long seriesId,
            @AuthenticationPrincipal User user) {

        SeriesDetailsDto details = seriesDetailsService.getDetailsForUser(user.getId(), seriesId);
        return ResponseEntity.ok(details);
    }

    @GetMapping("/search")
    public ResponseEntity<List<SeriesSearchResultDto>> searchSeries(@RequestParam String q) {
        return ResponseEntity.ok(tvMazeService.searchShows(q));
    }

}
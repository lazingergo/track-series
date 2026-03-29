package com.trackseries.controller;

import com.trackseries.dto.SeriesDetailsDto;
import com.trackseries.dto.SeriesSearchResultDto;
import com.trackseries.entity.Series;
import com.trackseries.enums.WatchStatus;
import com.trackseries.repository.SeriesRepository;
import com.trackseries.service.SeriesDetailsService;
import com.trackseries.service.TrackedSeriesService;
import com.trackseries.service.TvMazeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/series")
public class SeriesController {
    private static final Logger log = LoggerFactory.getLogger(SeriesController.class);

    private final TvMazeService tvMazeService;
    private final SeriesRepository seriesRepository;
    private final SeriesDetailsService seriesDetailsService;
    private final TrackedSeriesService trackedSeriesService;

    public SeriesController(TvMazeService tvMazeService,
                            SeriesRepository seriesRepository,
                            SeriesDetailsService seriesDetailsService,
                            TrackedSeriesService trackedSeriesService) {
        this.tvMazeService = tvMazeService;
        this.seriesRepository = seriesRepository;
        this.seriesDetailsService = seriesDetailsService;
        this.trackedSeriesService = trackedSeriesService;
    }

    @PostMapping("/add-to-collection/{tvMazeId}")
    public ResponseEntity<?> addToCollection(
            @PathVariable Long tvMazeId,
            Authentication authentication
    ) {
        log.debug(
            "/api/series/add-to-collection called, username='{}', tvMazeId={}",
            authentication.getName(),
            tvMazeId
        );
        Series series = trackedSeriesService.addSeriesToCollectionForUsername(authentication.getName(), tvMazeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(series);
    }

    // Get all series
    @GetMapping
    public ResponseEntity<List<Series>> getAllSeries() {
        List<Series> seriesList = seriesRepository.findAll();
        return ResponseEntity.ok(seriesList);
    }

    // Get series with pagination for large datasets
    @GetMapping("/paged")
    public ResponseEntity<Page<Series>> getSeriesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(Math.max(page, 0), safeSize);
        return ResponseEntity.ok(seriesRepository.findAll(pageable));
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
            Authentication authentication) {
        log.debug("/api/series/{}/details called, username='{}'", seriesId, authentication.getName());
        SeriesDetailsDto details = seriesDetailsService.getDetailsForUsername(authentication.getName(), seriesId);
        return ResponseEntity.ok(details);
    }

    @GetMapping("/search")
    public ResponseEntity<List<SeriesSearchResultDto>> searchSeries(
            @RequestParam String q,
            Authentication authentication
    ) {
        log.debug("/api/series/search called, query='{}'", q);
        return ResponseEntity.ok(tvMazeService.searchShows(q, authentication.getName()));
    }

}
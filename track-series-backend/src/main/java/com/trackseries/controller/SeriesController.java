package com.trackseries.controller;

import com.trackseries.entity.Series;
import com.trackseries.service.TvMazeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/series")
public class SeriesController {

    private final TvMazeService tvMazeService;

    public SeriesController(TvMazeService tvMazeService) {
        this.tvMazeService = tvMazeService;
    }

    @PostMapping("/import/{tvMazeId}")
    public ResponseEntity<Series> importSeries(@PathVariable Long tvMazeId) {
        Series savedSeries = tvMazeService.fetchAndSaveSeries(tvMazeId);

        return ResponseEntity.ok(savedSeries);
    }
}

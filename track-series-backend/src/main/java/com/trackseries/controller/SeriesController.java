package com.trackseries.controller;

import com.trackseries.dto.SeriesDetailsDto;
import com.trackseries.entity.Series;
import com.trackseries.repository.SeriesRepository;
import com.trackseries.service.SeriesDetailsService;
import com.trackseries.service.TvMazeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/series")
public class SeriesController {

    private final TvMazeService tvMazeService;
    private final SeriesRepository seriesRepository;
    private final SeriesDetailsService seriesDetailsService;

    public SeriesController(TvMazeService tvMazeService,
                            SeriesRepository seriesRepository,
                            SeriesDetailsService seriesDetailsService) {
        this.tvMazeService = tvMazeService;
        this.seriesRepository = seriesRepository;
        this.seriesDetailsService = seriesDetailsService;
    }

    // import a sereis form tvMaze
    @PostMapping("/import/{tvMazeId}")
    public ResponseEntity<Series> importSeries(@PathVariable Long tvMazeId) {
        Series savedSeries = tvMazeService.fetchAndSaveSeries(tvMazeId);

        return ResponseEntity.ok(savedSeries);
    }

    // get all series
    @GetMapping
    public ResponseEntity<List<Series>> getAllSeries() {
        List<Series> seriesList = seriesRepository.findAll();
        return ResponseEntity.ok(seriesList);
    }

    // getSeries by ID
    @GetMapping("/{id}")
    public ResponseEntity<Series> getSeriesById(@PathVariable Long id) {
        return seriesRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build()); // 404 if the id doesn't exist
    }

    @GetMapping("/users/{userId}/series/{seriesId}/details")
    public ResponseEntity<SeriesDetailsDto> getSeriesDetailsForUser(
            @PathVariable Long userId,
            @PathVariable Long seriesId) {

        SeriesDetailsDto details = seriesDetailsService.getDetailsForUser(userId, seriesId);
        return ResponseEntity.ok(details);
    }


}

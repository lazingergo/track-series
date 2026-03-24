package com.trackseries.service;

import com.trackseries.entity.Series;
import com.trackseries.entity.TrackedSeries;
import com.trackseries.entity.User;
import com.trackseries.enums.WatchStatus;
import com.trackseries.repository.SeriesRepository;
import com.trackseries.repository.TrackedSeriesRepository;
import com.trackseries.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TrackedSeriesService {
    private static final Logger log = LoggerFactory.getLogger(TrackedSeriesService.class);

    private final TrackedSeriesRepository trackedSeriesRepository;
    private final UserRepository userRepository;
    private final SeriesRepository seriesRepository;
    private final TvMazeService tvMazeService;

    public TrackedSeriesService(TrackedSeriesRepository trackedSeriesRepository,
                                UserRepository userRepository,
                                SeriesRepository seriesRepository,
                                TvMazeService tvMazeService) {
        this.trackedSeriesRepository = trackedSeriesRepository;
        this.userRepository = userRepository;
        this.seriesRepository = seriesRepository;
        this.tvMazeService = tvMazeService;
    }

    @Transactional
    public TrackedSeries addOrUpdateCollection(Long userId, Long seriesId, WatchStatus status) {
        log.debug("Add/update collection called, userId={}, seriesId={}, status={}", userId, seriesId, status);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User cannot find with this id " + userId));

        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new RuntimeException("Series cannot find with this id " + seriesId));

        // chack if the series is already in the list
        TrackedSeries tracked = trackedSeriesRepository.findByUserIdAndSeriesId(userId, seriesId)
                .orElse(new TrackedSeries());

        tracked.setUser(user);
        tracked.setSeries(series);
        tracked.setStatus(status);

        TrackedSeries saved = trackedSeriesRepository.save(tracked);
        log.info("Collection updated, userId={}, seriesId={}, status={}", userId, seriesId, status);
        return saved;
    }

    @Transactional
    public Series addCollectionByUsername(String username, Long tvMazeId) {
        return addCollectionByUsername(username, tvMazeId, WatchStatus.PLAN_TO_WATCH);
    }

    @Transactional
    public Series addCollectionByUsername(String username, Long tvMazeId, WatchStatus status) {
        log.debug("Add collection called, username='{}', tvMazeId={}, status={}", username, tvMazeId, status);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User cannot find with this username " + username));

        Series series = seriesRepository.findById(tvMazeId)
                .orElseGet(() -> {
                    log.debug("Download series from TVMaze, tvMazeId={}", tvMazeId);
                    Series savedSeries = tvMazeService.fetchAndSaveSeries(tvMazeId);
                    if (savedSeries == null) {
                        throw new IllegalStateException("Series import failed");
                    }
                    return savedSeries;
                });

        Optional<TrackedSeries> existing = trackedSeriesRepository.findByUserIdAndSeriesId(user.getId(), series.getId());
        if (existing.isPresent()) {
            throw new IllegalStateException("Series already added to collection");
        }

        TrackedSeries tracked = new TrackedSeries();
        tracked.setUser(user);
        tracked.setSeries(series);
        tracked.setStatus(status);
        trackedSeriesRepository.save(tracked);

        log.info("Collection added, userId={}, seriesId={}, status={}", user.getId(), series.getId(), status);
        return series;
    }

    @Transactional
    public TrackedSeries updateSeriesStatusByUsername(String username, Long seriesId, WatchStatus status) {
        log.debug("Update status called, username='{}', seriesId={}, status={}", username, seriesId, status);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User cannot find with this username " + username));

        TrackedSeries tracked = trackedSeriesRepository.findByUserIdAndSeriesId(user.getId(), seriesId)
                .orElseThrow(() -> new RuntimeException("Series not found in user collection"));

        tracked.setStatus(status);
        TrackedSeries saved = trackedSeriesRepository.save(tracked);
        log.info("Collection status updated, userId={}, seriesId={}, status={}", user.getId(), seriesId, status);
        return saved;
    }

    @Transactional
    public  TrackedSeries updateRating(Long userId, Long seriesId, Integer rating) {
        if (rating != null && (rating < 1 || rating > 10)) {
            log.warn("Invalid rating value={}, userId={}, seriesId={}", rating, userId, seriesId);
            throw new IllegalArgumentException("Rating must be between 1 and 10");
        }

        TrackedSeries tracked = trackedSeriesRepository.findByUserIdAndSeriesId(userId, seriesId)
                .orElseThrow(() -> new RuntimeException("Series not found in user collection. Please add it first."));

        tracked.setRating(rating);
        TrackedSeries saved = trackedSeriesRepository.save(tracked);
        log.info("Rating updated, userId={}, seriesId={}, rating={}", userId, seriesId, rating);
        return saved;
    }

}

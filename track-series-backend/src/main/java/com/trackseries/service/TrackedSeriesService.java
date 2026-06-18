package com.trackseries.service;

import com.trackseries.entity.Series;
import com.trackseries.entity.TrackedSeries;
import com.trackseries.entity.User;
import com.trackseries.enums.WatchStatus;
import com.trackseries.exception.BadRequestException;
import com.trackseries.exception.ConflictException;
import com.trackseries.exception.ResourceNotFoundException;
import com.trackseries.repository.SeriesRepository;
import com.trackseries.repository.TrackedSeriesRepository;
import com.trackseries.repository.UserRepository;
import com.trackseries.repository.WatchedEpisodeRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TrackedSeriesService {
    private static final Logger log = LoggerFactory.getLogger(TrackedSeriesService.class);

    private final TrackedSeriesRepository trackedSeriesRepository;
    private final UserRepository userRepository;
    private final SeriesRepository seriesRepository;
    private final TvMazeService tvMazeService;
    private final WatchedEpisodeRepository watchedEpisodeRepository;

    public TrackedSeriesService(TrackedSeriesRepository trackedSeriesRepository,
                                UserRepository userRepository,
                                SeriesRepository seriesRepository,
                                TvMazeService tvMazeService,
                                WatchedEpisodeRepository watchedEpisodeRepository) {
        this.trackedSeriesRepository = trackedSeriesRepository;
        this.userRepository = userRepository;
        this.seriesRepository = seriesRepository;
        this.tvMazeService = tvMazeService;
        this.watchedEpisodeRepository = watchedEpisodeRepository;
    }

    @Transactional
    public TrackedSeries addOrUpdateCollection(Long userId, Long seriesId, WatchStatus status) {
        log.debug("Add/update collection called, userId={}, seriesId={}, status={}", userId, seriesId, status);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User cannot find with this id " + userId));

        Series series = seriesRepository.findById(seriesId)
            .orElseThrow(() -> new ResourceNotFoundException("Series cannot find with this id " + seriesId));

        // chack if the series is already in the list
        TrackedSeries tracked = trackedSeriesRepository.findByUserIdAndSeriesId(userId, seriesId)
                .orElse(new TrackedSeries());

        tracked.setUser(user);
        tracked.setSeries(series);
        tracked.setStatus(status);

        try {
            TrackedSeries saved = trackedSeriesRepository.save(tracked);
            log.info("Collection updated, userId={}, seriesId={}, status={}", userId, seriesId, status);
            return saved;
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Series already added to collection");
        }
    }

    @Transactional
    public Series addSeriesToCollectionForUsername(String username, Long tvMazeId) {
        return addSeriesToCollectionForUsername(username, tvMazeId, WatchStatus.PLAN_TO_WATCH);
    }

    @Transactional
    public Series addSeriesToCollectionForUsername(String username, Long tvMazeId, WatchStatus status) {
        log.debug("Add collection called, username='{}', tvMazeId={}, status={}", username, tvMazeId, status);

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User cannot find with this username " + username));

        Series series = seriesRepository.findById(tvMazeId)
                .orElseGet(() -> {
                    log.debug("Download series from TVMaze, tvMazeId={}", tvMazeId);
                    Series savedSeries = tvMazeService.fetchAndSaveSeries(tvMazeId);
                    if (savedSeries == null) {
                        throw new ConflictException("Series import failed");
                    }
                    return savedSeries;
                });

        Optional<TrackedSeries> existing = trackedSeriesRepository.findByUserIdAndSeriesId(
            user.getId(),
            series.getId()
        );
        if (existing.isPresent()) {
            throw new ConflictException("Series already added to collection");
        }

        TrackedSeries tracked = new TrackedSeries();
        tracked.setUser(user);
        tracked.setSeries(series);
        tracked.setStatus(status);
        try {
            trackedSeriesRepository.save(tracked);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Series already added to collection");
        }

        log.info("Collection added, userId={}, seriesId={}, status={}", user.getId(), series.getId(), status);
        return series;
    }

    @Transactional
    public TrackedSeries updateSeriesStatusForUsername(String username, Long seriesId, WatchStatus status) {
        log.debug("Update status called, username='{}', seriesId={}, status={}", username, seriesId, status);

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User cannot find with this username " + username));

        TrackedSeries tracked = trackedSeriesRepository.findByUserIdAndSeriesId(user.getId(), seriesId)
            .orElseThrow(() -> new ResourceNotFoundException("Series not found in user collection"));

        tracked.setStatus(status);
        TrackedSeries saved = trackedSeriesRepository.save(tracked);
        log.info("Collection status updated, userId={}, seriesId={}, status={}", user.getId(), seriesId, status);
        return saved;
    }

    @Transactional
    public  TrackedSeries updateRating(Long userId, Long seriesId, Integer rating) {
        if (rating != null && (rating < 1 || rating > 10)) {
            log.warn("Invalid rating value={}, userId={}, seriesId={}", rating, userId, seriesId);
            throw new BadRequestException("Rating must be between 1 and 10");
        }

        TrackedSeries tracked = trackedSeriesRepository.findByUserIdAndSeriesId(userId, seriesId)
            .orElseThrow(
                () -> new ResourceNotFoundException(
                    "Series not found in user collection. Please add it first."
                )
            );

        tracked.setRating(rating);
        TrackedSeries saved = trackedSeriesRepository.save(tracked);
        log.info("Rating updated, userId={}, seriesId={}, rating={}", userId, seriesId, rating);
        return saved;
    }

    @Transactional
    public TrackedSeries updateSeriesRatingForUsername(String username, Long seriesId, Integer rating) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(
                () -> new ResourceNotFoundException(
                    "User cannot find with this username " + username
                )
            );
        return updateRating(user.getId(), seriesId, rating);
    }

    @Transactional
    public void removeSeriesFromCollectionForUsername(String username, Long seriesId) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User cannot find with this username " + username));

        TrackedSeries tracked = trackedSeriesRepository.findByUserIdAndSeriesId(user.getId(), seriesId)
            .orElseThrow(() -> new ResourceNotFoundException("Series not found in user collection"));

        watchedEpisodeRepository.deleteByUserIdAndEpisode_Series_Id(user.getId(), seriesId);
        trackedSeriesRepository.delete(tracked);

        log.info("Series removed from collection, userId={}, seriesId={}", user.getId(), seriesId);
    }

    @Transactional
    public TrackedSeries refreshTrackedSeriesForUsername(String username, Long seriesId) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User cannot find with this username " + username));

        TrackedSeries tracked = trackedSeriesRepository.findByUserIdAndSeriesId(user.getId(), seriesId)
            .orElseThrow(() -> new ResourceNotFoundException("Series not found in user collection"));

        tvMazeService.refreshSeriesEpisodes(seriesId);
        syncCompletedStatusAfterRefresh(user.getId(), tracked);
        return trackedSeriesRepository.save(tracked);
    }

    @Transactional
    public List<TrackedSeries> refreshOngoingTrackedSeriesForUsername(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User cannot find with this username " + username));

        List<TrackedSeries> trackedSeriesList = trackedSeriesRepository.findByUserId(user.getId());
        List<TrackedSeries> refreshed = new ArrayList<>();

        for (TrackedSeries tracked : trackedSeriesList) {
            if (isSeriesOngoing(tracked.getSeries())) {
                tvMazeService.refreshSeriesEpisodes(tracked.getSeries().getId());
                syncCompletedStatusAfterRefresh(user.getId(), tracked);
                refreshed.add(trackedSeriesRepository.save(tracked));
            }
        }

        return refreshed;
    }

    @Transactional
    public List<Long> activeSeries(Long userId) {
        return trackedSeriesRepository.findDistinctSeriesIdsByUserIdAndStatusIn(
            userId,
            List.of(WatchStatus.WATCHING, WatchStatus.PLAN_TO_WATCH)
        );
    }

    @Transactional
    public List<Long> activeSeriesForUsername(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User cannot find with this username " + username));

        return activeSeries(user.getId());
    }

    private boolean isSeriesOngoing(Series series) {
        if (series.getEnded() != null) {
            return false;
        }

        String status = series.getStatus();
        return status == null || !"ended".equalsIgnoreCase(status);
    }

    private void syncCompletedStatusAfterRefresh(Long userId, TrackedSeries tracked) {
        if (tracked.getStatus() != WatchStatus.COMPLETED) {
            return;
        }

        Long seriesId = tracked.getSeries().getId();
        long totalEpisodes = episodeCountForSeries(seriesId);
        long watchedEpisodes = watchedEpisodeRepository.countNextEpisodes(userId, seriesId, 0);

        if (watchedEpisodes < totalEpisodes) {
            tracked.setStatus(watchedEpisodes > 0 ? WatchStatus.WATCHING : WatchStatus.PLAN_TO_WATCH);
            log.info(
                "Status changed after refresh, userId={}, seriesId={}, oldStatus={}, newStatus={}",
                userId,
                seriesId,
                WatchStatus.COMPLETED,
                tracked.getStatus()
            );
        }
    }

    private long episodeCountForSeries(Long seriesId) {
        return seriesRepository.findById(seriesId)
            .map(series -> series.getEpisodes().stream().filter(ep -> ep.getSeasonNumber() != null && ep.getSeasonNumber() > 0)
                .count())
            .orElse(0L);
    }

}

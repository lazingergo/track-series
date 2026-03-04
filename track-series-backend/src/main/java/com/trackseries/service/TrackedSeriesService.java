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

@Service
public class TrackedSeriesService {
    private static final Logger log = LoggerFactory.getLogger(TrackedSeriesService.class);

    private final TrackedSeriesRepository trackedSeriesRepository;
    private final UserRepository userRepository;
    private final SeriesRepository seriesRepository;

    public TrackedSeriesService(TrackedSeriesRepository trackedSeriesRepository,
                                UserRepository userRepository,
                                SeriesRepository seriesRepository) {
        this.trackedSeriesRepository = trackedSeriesRepository;
        this.userRepository = userRepository;
        this.seriesRepository = seriesRepository;
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
    public TrackedSeries addOrUpdateCollectionByUsername(String username, Long seriesId, WatchStatus status) {
        log.debug("Add/update collection by username='{}', seriesId={}, status={}", username, seriesId, status);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User cannot find with this username " + username));

        return addOrUpdateCollection(user.getId(), seriesId, status);
    }

    @Transactional
    public  TrackedSeries updateRating(Long userId, Long seriesId, Integer rating) {
        if (rating != null && rating < 1 || rating > 10) {
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

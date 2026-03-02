package com.trackseries.service;

import com.trackseries.entity.Series;
import com.trackseries.entity.TrackedSeries;
import com.trackseries.entity.User;
import com.trackseries.enums.WatchStatus;
import com.trackseries.repository.SeriesRepository;
import com.trackseries.repository.TrackedSeriesRepository;
import com.trackseries.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class TrackedSeriesService {

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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User cannot find with this id " + userId));

        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new RuntimeException("Series cannot find with this id " + seriesId));

        // chack if the series is already in the list

        TrackedSeries tracked = trackedSeriesRepository.findByUserIdAndSeriesId(userId,seriesId)
                .orElse(new TrackedSeries());

        tracked.setUser(user);
        tracked.setSeries(series);
        tracked.setStatus(status);

        return trackedSeriesRepository.save(tracked);

    }
}

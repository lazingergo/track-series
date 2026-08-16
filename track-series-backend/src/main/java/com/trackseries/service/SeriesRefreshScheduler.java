package com.trackseries.service;

import com.trackseries.entity.Series;
import com.trackseries.enums.WatchStatus;
import com.trackseries.repository.SeriesRepository;
import com.trackseries.repository.TrackedSeriesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeriesRefreshScheduler {
    private static final Logger log = LoggerFactory.getLogger(SeriesRefreshScheduler.class);

    private final TrackedSeriesRepository trackedSeriesRepository;
    private final SeriesRepository seriesRepository;
    private final TvMazeService tvMazeService;

    @Value("${app.series-refresh.delay-ms:1000}")
    private long delayMs;

    public SeriesRefreshScheduler(TrackedSeriesRepository trackedSeriesRepository,
                                  SeriesRepository seriesRepository,
                                  TvMazeService tvMazeService) {
        this.trackedSeriesRepository = trackedSeriesRepository;
        this.seriesRepository = seriesRepository;
        this.tvMazeService = tvMazeService;
    }

    @Scheduled(cron = "${app.series-refresh.cron:0 0 4 * * MON}", zone = "UTC")
    public void refreshTrackedSeriesWeekly() {
        List<Long> seriesIds = trackedSeriesRepository.findDistinctSeriesIdsByStatusIn(
            List.of(WatchStatus.WATCHING, WatchStatus.PLAN_TO_WATCH)
        );

        log.info("Weekly refresh started, seriesCount={}", seriesIds.size());

        int ok = 0;
        int failed = 0;
        int skipped = 0;

        for (Long seriesId : seriesIds) {
            if (!isSeriesOngoing(seriesId)) {
                skipped++;
                log.debug("Skipping ended seriesId={}", seriesId);
                continue;
            }

            try {
                tvMazeService.refreshSeriesEpisodes(seriesId);
                ok++;
            } catch (Exception ex) {
                failed++;
                log.warn("Failed to refresh seriesId={}: {}", seriesId, ex.getMessage());
            }

            try {
                Thread.sleep(delayMs); // rate-limit védelem
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.warn("Scheduler interrupted");
                break;
            }
        }

        log.info("Weekly refresh finished, success={}, failed={}, skipped={}", ok, failed, skipped);
    }

    private boolean isSeriesOngoing(Long seriesId) {
        return seriesRepository.findById(seriesId)
            .map(this::isSeriesOngoing)
            .orElse(false);
    }

    private boolean isSeriesOngoing(Series series) {
        if (series.getEnded() != null) {
            return false;
        }

        String status = series.getStatus();
        return status == null || !"ended".equalsIgnoreCase(status);
    }
}

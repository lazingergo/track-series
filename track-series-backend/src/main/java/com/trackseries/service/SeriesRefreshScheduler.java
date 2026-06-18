package com.trackseries.service;

import com.trackseries.enums.WatchStatus;
import com.trackseries.repository.TrackedSeriesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

public class SeriesRefreshScheduler {
    private static final Logger log = LoggerFactory.getLogger(SeriesRefreshScheduler.class);

    private final TrackedSeriesRepository trackedSeriesRepository;
    private final TvMazeService tvMazeService;

    @Value("${app.service-refresh.delay-ms:1000}")
    private long delayMs;

    public SeriesRefreshScheduler(TrackedSeriesRepository trackedSeriesRepository,
                                  TvMazeService tvMazeService) {
        this.trackedSeriesRepository=trackedSeriesRepository;
        this.tvMazeService=tvMazeService;
    }

    @Scheduled(cron = "${app.series-refresh.cron:0 0 4 * * MON}", zone = "UTC")
    public void refreshTrackedSeriesWeekly() {
        List<Long> seriesIds = trackedSeriesRepository.findDistinctSeriesIdsByStatusIn(
            List.of(WatchStatus.WATCHING, WatchStatus.PLAN_TO_WATCH)
        );

        log.info("Weekly refresh started, seriesCount={}", seriesIds.size());

        int ok = 0;
        int failed = 0;

        for (Long seriesId : seriesIds) {
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

        log.info("Weekly refresh finished, success={}, failed={}", ok, failed);
    }
}

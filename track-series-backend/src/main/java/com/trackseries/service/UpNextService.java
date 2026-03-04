package com.trackseries.service;

import com.trackseries.dto.UpNextDto;
import com.trackseries.entity.Episode;
import com.trackseries.entity.TrackedSeries;
import com.trackseries.entity.User;
import com.trackseries.entity.WatchedEpisode;
import com.trackseries.enums.WatchStatus;
import com.trackseries.repository.EpisodeRepository;
import com.trackseries.repository.TrackedSeriesRepository;
import com.trackseries.repository.UserRepository;
import com.trackseries.repository.WatchedEpisodeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class UpNextService {
    private static final int INACTIVE_DAYS_THRESHOLD = 14;

    private final TrackedSeriesRepository trackedSeriesRepository;
    private final EpisodeRepository episodeRepository;
    private final UserRepository userRepository;
    private final WatchedEpisodeRepository watchedEpisodeRepository;

    public UpNextService(TrackedSeriesRepository trackedSeriesRepository,
                         EpisodeRepository episodeRepository,
                         UserRepository userRepository,
                         WatchedEpisodeRepository watchedEpisodeRepository) {
        this.trackedSeriesRepository = trackedSeriesRepository;
        this.episodeRepository = episodeRepository;
        this.userRepository = userRepository;
        this.watchedEpisodeRepository = watchedEpisodeRepository;
    }

    public UpNextDto getUpNextForUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User cannot find with this username " + username));

        return getUpNextForUser(user.getId());
    }

    public UpNextDto getUpNextForUser(Long userId) {
        UpNextDto response = new UpNextDto();
        response.setWatching(new ArrayList<>());
        response.setPlanToWatch(new ArrayList<>());
        response.setNotWatchedForAWhile(new ArrayList<>());

        LocalDateTime inactivityThreshold = LocalDateTime.now().minusDays(INACTIVE_DAYS_THRESHOLD);

        // get all of the users series
        List<TrackedSeries> allTracked = trackedSeriesRepository.findByUserId(userId);

        for (TrackedSeries tracked : allTracked) {
            if (tracked.getStatus() == WatchStatus.WATCHING || tracked.getStatus() == WatchStatus.PLAN_TO_WATCH) {
                UpNextDto.NextEpisodeItem item = calculateNextEpisode(tracked);

                if (item != null) {
                    if (tracked.getStatus() == WatchStatus.WATCHING) {
                        if (isNotWatchedForAWhile(userId, tracked.getSeries().getId(), inactivityThreshold)) {
                            response.getNotWatchedForAWhile().add(item);
                        } else {
                            response.getWatching().add(item);
                        }
                    } else {
                        response.getPlanToWatch().add(item);
                    }
                }
            }
        }

        Comparator<UpNextDto.NextEpisodeItem> relevanceComparator = Comparator
                .comparing(UpNextDto.NextEpisodeItem::getSeasonNumber, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(UpNextDto.NextEpisodeItem::getEpisodeNumber, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(UpNextDto.NextEpisodeItem::getSeriesTitle, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));

        response.getWatching().sort(relevanceComparator);
        response.getNotWatchedForAWhile().sort(relevanceComparator);
        response.getPlanToWatch().sort(relevanceComparator);

        return response;
    }

    private boolean isNotWatchedForAWhile(Long userId, Long seriesId, LocalDateTime threshold) {
        List<WatchedEpisode> watchedEpisodes = watchedEpisodeRepository.findByUserIdAndEpisode_Series_Id(userId, seriesId);
        LocalDateTime latestWatchedAt = watchedEpisodes.stream()
                .map(WatchedEpisode::getWatchedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return latestWatchedAt != null && latestWatchedAt.isBefore(threshold);
    }

    private UpNextDto.NextEpisodeItem calculateNextEpisode(TrackedSeries tracked) {
        Long seriesId = tracked.getSeries().getId();

        // get all normal epizode (season > 0), sorted
        List<Episode> validEpisodes = episodeRepository.findNextEpisodes(seriesId, 0);

        if (validEpisodes.isEmpty()) {
            return null;
        }

        // get the latest watched episode
        Episode highestWatched = null;
        for (WatchedEpisode we : tracked.getUser().getWatchedEpisodes()) {
            if (we.getEpisode().getSeries().getId().equals(seriesId)) {
                if (highestWatched == null || isAfter(we.getEpisode(), highestWatched)) {
                    highestWatched = we.getEpisode();
                }
            }
        }

        // select next episode
        Episode nextEpisode = null;
        if (highestWatched == null) {
            // 1. episode if plan to watch
            nextEpisode = validEpisodes.get(0);
        } else {
            // search the first unwatched episode
            for (Episode ep : validEpisodes) {
                if (isAfter(ep, highestWatched)) {
                    nextEpisode = ep;
                    break;
                }
            }
        }


        // if there is a next episode add to dto
        if (nextEpisode != null) {
            UpNextDto.NextEpisodeItem item = new UpNextDto.NextEpisodeItem();
            item.setSeriesId(seriesId);
            item.setSeriesTitle(tracked.getSeries().getTitle());
            item.setImageUrl(tracked.getSeries().getImageUrl());
            item.setNextEpisodeId(nextEpisode.getId());
            item.setSeasonNumber(nextEpisode.getSeasonNumber());
            item.setEpisodeNumber(nextEpisode.getEpisodeNumber());
            item.setEpisodeTitle(nextEpisode.getTitle());
            return item;
        }

        return null;
    }

    private boolean isAfter(Episode ep1, Episode ep2) {
        if (ep1.getSeasonNumber() > ep2.getSeasonNumber()) {
            return true;
        }
        return ep1.getSeasonNumber().equals(ep2.getSeasonNumber()) && ep1.getEpisodeNumber() > ep2.getEpisodeNumber();
    }
}


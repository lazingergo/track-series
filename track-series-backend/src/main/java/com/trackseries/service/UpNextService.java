package com.trackseries.service;

import com.trackseries.dto.UpNextDto;
import com.trackseries.entity.Episode;
import com.trackseries.entity.TrackedSeries;
import com.trackseries.entity.User;
import com.trackseries.entity.WatchedEpisode;
import com.trackseries.enums.WatchStatus;
import com.trackseries.exception.ResourceNotFoundException;
import com.trackseries.repository.EpisodeRepository;
import com.trackseries.repository.TrackedSeriesRepository;
import com.trackseries.repository.UserRepository;
import com.trackseries.repository.WatchedEpisodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UpNextService {
    private static final int INACTIVE_DAYS_THRESHOLD = 14;
    private static final Logger log = LoggerFactory.getLogger(UpNextService.class);

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
        log.debug("Up-next requested for username='{}'", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User cannot find with this username " + username));

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

        if (allTracked.isEmpty()) {
            return response;
        }

        List<Long> seriesIds = allTracked.stream()
                .map(t -> t.getSeries().getId())
                .distinct()
                .toList();

        List<Episode> allEpisodesForTrackedSeries = episodeRepository.findNextEpisodesForSeriesIds(seriesIds, 0);
        Map<Long, List<Episode>> episodesBySeriesId = allEpisodesForTrackedSeries.stream()
                .collect(Collectors.groupingBy(ep -> ep.getSeries().getId(), HashMap::new, Collectors.toList()));

        Map<Long, Long> episodeIdToSeriesId = new HashMap<>();
        for (Episode episode : allEpisodesForTrackedSeries) {
            episodeIdToSeriesId.put(episode.getId(), episode.getSeries().getId());
        }

        List<WatchedEpisode> allWatchedForTrackedSeries = watchedEpisodeRepository.findByUserIdAndEpisode_Series_IdIn(userId, seriesIds);
        Map<Long, List<WatchedEpisode>> watchedBySeriesId = new HashMap<>();
        for (WatchedEpisode watchedEpisode : allWatchedForTrackedSeries) {
            Long watchedEpisodeId = watchedEpisode.getEpisode().getId();
            Long seriesId = episodeIdToSeriesId.get(watchedEpisodeId);
            if (seriesId == null) {
                continue;
            }
            watchedBySeriesId.computeIfAbsent(seriesId, ignored -> new ArrayList<>()).add(watchedEpisode);
        }

        for (TrackedSeries tracked : allTracked) {
            if (tracked.getStatus() == WatchStatus.WATCHING || tracked.getStatus() == WatchStatus.PLAN_TO_WATCH) {
                Long seriesId = tracked.getSeries().getId();
                List<Episode> validEpisodes = episodesBySeriesId.getOrDefault(seriesId, List.of());
                List<WatchedEpisode> watchedEpisodes = watchedBySeriesId.getOrDefault(seriesId, List.of());

                UpNextDto.NextEpisodeItem item = calculateNextEpisode(tracked, validEpisodes, watchedEpisodes);

                if (item != null) {
                    if (tracked.getStatus() == WatchStatus.WATCHING) {
                        if (isNotWatchedForAWhile(watchedEpisodes, inactivityThreshold)) {
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

        log.debug("Up-next generated, userId={}, watchingCount={}, planToWatchCount={}",
            userId,
            response.getWatching().size(),
            response.getPlanToWatch().size());
        return response;
    }

    private boolean isNotWatchedForAWhile(List<WatchedEpisode> watchedEpisodes, LocalDateTime threshold) {
        LocalDateTime latestWatchedAt = watchedEpisodes.stream()
                .map(WatchedEpisode::getWatchedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return latestWatchedAt != null && latestWatchedAt.isBefore(threshold);
    }

    private UpNextDto.NextEpisodeItem calculateNextEpisode(TrackedSeries tracked,
                                                           List<Episode> validEpisodes,
                                                           List<WatchedEpisode> watchedEpisodes) {
        Long seriesId = tracked.getSeries().getId();

        if (validEpisodes.isEmpty()) {
            return null;
        }

        Set<Long> watchedEpisodeIds = watchedEpisodes.stream()
                .map(w -> w.getEpisode().getId())
                .collect(Collectors.toCollection(HashSet::new));

        Episode highestWatched = null;
        for (Episode ep : validEpisodes) {
            if (watchedEpisodeIds.contains(ep.getId())) {
                if (highestWatched == null || isAfter(ep, highestWatched)) {
                    highestWatched = ep;
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


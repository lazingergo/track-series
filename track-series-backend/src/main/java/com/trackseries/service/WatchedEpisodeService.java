package com.trackseries.service;

import com.trackseries.entity.Episode;
import com.trackseries.entity.User;
import com.trackseries.entity.WatchedEpisode;
import com.trackseries.enums.WatchStatus;
import com.trackseries.exception.ResourceNotFoundException;
import com.trackseries.repository.EpisodeRepository;
import com.trackseries.repository.UserRepository;
import com.trackseries.repository.WatchedEpisodeRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class WatchedEpisodeService {
    private static final Logger log = LoggerFactory.getLogger(WatchedEpisodeService.class);

    private final WatchedEpisodeRepository watchedEpisodeRepository;
    private final EpisodeRepository episodeRepository;
    private final TrackedSeriesService trackedSeriesService;
    private final UserRepository userRepository;

    public WatchedEpisodeService(WatchedEpisodeRepository watchedEpisodeRepository,
                                 EpisodeRepository episodeRepository,
                                 TrackedSeriesService trackedSeriesService,
                                 UserRepository userRepository) {
        this.watchedEpisodeRepository = watchedEpisodeRepository;
        this.episodeRepository = episodeRepository;
        this.trackedSeriesService = trackedSeriesService;
        this.userRepository = userRepository;
    }

    @Transactional
    public void markEpisodeAsWatchedForUsername(
            String username,
            Long episodeId,
            boolean includePrevious,
            LocalDateTime customDate
    ) {
        log.debug("Mark watched by username='{}', episodeId={}, includePrevious={}, customDate={}",
                username,
                episodeId,
                includePrevious,
                customDate
        );
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User cannot find with this username " + username));
        markEpisodeAsWatched(user.getId(), episodeId, includePrevious, customDate);
    }

    @Transactional
    public void markEpisodeAsWatched(Long userId, Long episodeId, boolean includePrevious, LocalDateTime customDate) {
        log.debug(
            "Mark watched called, userId={}, episodeId={}, includePrevious={}",
            userId,
            episodeId,
            includePrevious
        );
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User cannot find with this id " + userId));
        Episode currentEpisode = episodeRepository.findById(episodeId)
            .orElseThrow(() -> new ResourceNotFoundException("Episode not found with id " + episodeId));
        Long seriesId = currentEpisode.getSeries().getId();
        LocalDateTime dateToSave = (customDate != null) ? customDate : LocalDateTime.now();

        if (includePrevious) {
            /*
                If we have already seen a series and mark a part, e.g. S02E03, as seen,
                    then it is possible to treat it as if we had automatically seen the
            */

            List<Long> candidateEpisodeIds = episodeRepository.findEpisodeIdsUpTo(
                    seriesId,
                    currentEpisode.getSeasonNumber(),
                    currentEpisode.getEpisodeNumber()
            );

            if (!candidateEpisodeIds.isEmpty()) {
                List<Long> existingIds = watchedEpisodeRepository.findExistingEpisodeIds(userId, candidateEpisodeIds);
                Set<Long> existingIdSet = new HashSet<>(existingIds);

                List<WatchedEpisode> toSave = new ArrayList<>();
                for (Long candidateEpisodeId : candidateEpisodeIds) {
                    if (existingIdSet.contains(candidateEpisodeId)) {
                        continue;
                    }

                    Episode episodeRef = episodeRepository.getReferenceById(candidateEpisodeId);

                    WatchedEpisode watched = new WatchedEpisode();
                    watched.setUser(user);
                    watched.setEpisode(episodeRef);
                    watched.setWatchedAt(dateToSave);
                    toSave.add(watched);
                }

                if (!toSave.isEmpty()) {
                    watchedEpisodeRepository.saveAll(toSave);
                }
            }
            log.info(
                    "Marked episode with previous episodes, userId={}, episodeId={}, seriesId={}",
                    userId,
                    episodeId,
                    seriesId
            );
        } else {
            // just save the marked episode
            saveWatchedIfNotExists(user, currentEpisode, dateToSave);
            log.info(
                    "Marked single episode as watched, userId={}, episodeId={}, seriesId={}",
                    userId,
                    episodeId,
                    seriesId
            );
        }

        updateSeriesStatus(userId, seriesId);
    }

    @Transactional
    public void unmarkEpisodeAsWatched(Long userId, Long episodeId) {
        log.debug("Unmark watched called, userId={}, episodeId={}", userId, episodeId);
        Optional<WatchedEpisode> watched = watchedEpisodeRepository.findByUserIdAndEpisodeId(userId, episodeId);
        watched.ifPresent(watchedEpisodeRepository::delete);

        Episode currentEpisode = episodeRepository.findById(episodeId)
            .orElseThrow(() -> new ResourceNotFoundException("Episode not found with id " + episodeId));
        updateSeriesStatus(userId, currentEpisode.getSeries().getId());
        log.info("Episode unmarked as watched, userId={}, episodeId={}", userId, episodeId);
    }

    @Transactional
    public void unmarkEpisodeAsWatchedForUsername(String username, Long episodeId) {
        log.debug("Unmark watched by username='{}', episodeId={}", username, episodeId);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User cannot find with this username " + username));
        unmarkEpisodeAsWatched(user.getId(), episodeId);
    }


    private void saveWatchedIfNotExists(User user, Episode episode, LocalDateTime watchedAt) {
        if (watchedEpisodeRepository.findByUserIdAndEpisodeId(user.getId(), episode.getId()).isEmpty()) {
            WatchedEpisode watched = new WatchedEpisode();
            watched.setUser(user);
            watched.setEpisode(episode);
            watched.setWatchedAt(watchedAt);
            watchedEpisodeRepository.save(watched);
        }
    }

    private void updateSeriesStatus(Long userId, Long seriesId) {
        // count all "normal" episode
        long totalEpisodes = episodeRepository.countBySeriesIdAndSeasonNumberGreaterThan(seriesId, 0);

        long watchedEpisodes = watchedEpisodeRepository.countNextEpisodes(userId, seriesId, 0);

        WatchStatus newStatus = WatchStatus.PLAN_TO_WATCH;

        if (watchedEpisodes > 0 && watchedEpisodes < totalEpisodes) {
            newStatus = WatchStatus.WATCHING;
        } else if (watchedEpisodes > 0 && watchedEpisodes >= totalEpisodes) {
            newStatus = WatchStatus.COMPLETED;
        }

        trackedSeriesService.addOrUpdateCollection(userId, seriesId, newStatus);
        log.debug("Series status recalculated, userId={}, seriesId={}, status={}", userId, seriesId, newStatus);
    }


}

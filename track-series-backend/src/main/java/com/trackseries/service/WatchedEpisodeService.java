package com.trackseries.service;

import com.trackseries.entity.Episode;
import com.trackseries.entity.User;
import com.trackseries.entity.WatchedEpisode;
import com.trackseries.enums.WatchStatus;
import com.trackseries.repository.EpisodeRepository;
import com.trackseries.repository.UserRepository;
import com.trackseries.repository.WatchedEpisodeRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WatchedEpisodeService {
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
    public void markEpisodeAsWatched(Long userId, Long episodeId, boolean includePrevious, LocalDateTime customDate) {
        User user = userRepository.findById(userId).orElseThrow();
        Episode currentEpisode = episodeRepository.findById(episodeId).orElseThrow();
        Long seriesId = currentEpisode.getSeries().getId();
        LocalDateTime dateToSave = (customDate != null) ? customDate : LocalDateTime.now();

        if (includePrevious) {
            /*
                If we have already seen a series and mark a part, e.g. S02E03, as seen,
                    then it is possible to treat it as if we had automatically seen the
            */

            // get all episode
            List<Episode> allEpisodes = episodeRepository.findBySeriesId(seriesId);

            for (Episode ep : allEpisodes) {
                if (ep.getSeasonNumber() > 0 && isBeforeOrEqual(ep, currentEpisode)){
                    saveWatchedIfNotExists(user, ep, dateToSave);
                }
            }
        } else {
            // just save the marked episode
            saveWatchedIfNotExists(user, currentEpisode, dateToSave);
        }

        updateSeriesStatus(userId, seriesId);
    }

    @Transactional
    public void unmarkEpisodeAsWatched(Long userId, Long episodeId) {
        Optional<WatchedEpisode> watched = watchedEpisodeRepository.findByUserIdAndEpisodeId(userId, episodeId);
        watched.ifPresent(watchedEpisodeRepository::delete);

        Episode currentEpisode = episodeRepository.findById(episodeId).orElseThrow();
        updateSeriesStatus(userId, currentEpisode.getSeries().getId());
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

    private boolean isBeforeOrEqual(Episode ep, Episode target) {
        if (ep.getSeasonNumber() < target.getSeasonNumber()) {
            return true;
        }
        return ep.getSeasonNumber().equals(target.getSeasonNumber())
                && ep.getEpisodeNumber() <= target.getEpisodeNumber();
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
    }


}

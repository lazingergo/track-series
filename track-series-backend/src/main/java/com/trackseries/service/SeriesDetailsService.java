package com.trackseries.service;

import com.trackseries.dto.SeriesDetailsDto;
import com.trackseries.entity.Episode;
import com.trackseries.entity.Series;
import com.trackseries.entity.TrackedSeries;
import com.trackseries.entity.User;
import com.trackseries.entity.WatchedEpisode;
import com.trackseries.exception.ResourceNotFoundException;
import com.trackseries.repository.EpisodeRepository;
import com.trackseries.repository.SeriesRepository;
import com.trackseries.repository.TrackedSeriesRepository;
import com.trackseries.repository.UserRepository;
import com.trackseries.repository.WatchedEpisodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SeriesDetailsService {
    private static final Logger log = LoggerFactory.getLogger(SeriesDetailsService.class);

    private final SeriesRepository seriesRepository;
    private final TrackedSeriesRepository trackedSeriesRepository;
    private final EpisodeRepository episodeRepository;
    private final WatchedEpisodeRepository watchedEpisodeRepository;
    private final UserRepository userRepository;

    public SeriesDetailsService(SeriesRepository seriesRepository,
                                TrackedSeriesRepository trackedSeriesRepository,
                                EpisodeRepository episodeRepository,
                                WatchedEpisodeRepository watchedEpisodeRepository,
                                UserRepository userRepository) {
        this.seriesRepository = seriesRepository;
        this.trackedSeriesRepository = trackedSeriesRepository;
        this.episodeRepository = episodeRepository;
        this.watchedEpisodeRepository = watchedEpisodeRepository;
        this.userRepository = userRepository;
    }

    public SeriesDetailsDto getDetailsForUsername(String username, Long seriesId) {
        log.debug("Series details requested, username='{}', seriesId={}", username, seriesId);
        User user = userRepository.findByUsername(username)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "User cannot find with this username " + username
                        )
                );
        return getDetailsForUser(user.getId(), seriesId);
    }

    public SeriesDetailsDto getDetailsForUser(Long userId, Long seriesId) {
        Series series = seriesRepository.findById(seriesId)
            .orElseThrow(() -> new ResourceNotFoundException("Series not found with id " + seriesId));

        SeriesDetailsDto dto = new SeriesDetailsDto();
        dto.setId(series.getId());
        dto.setTitle(series.getTitle());
        dto.setSummary(series.getSummary());
        dto.setImageUrl(series.getImageUrl());

        // get user series status
        Optional<TrackedSeries> tracked = trackedSeriesRepository.findByUserIdAndSeriesId(userId, seriesId);
        if (tracked.isPresent()) {
            dto.setUserStatus(tracked.get().getStatus());
            dto.setUserRating(tracked.get().getRating());
        }

        // watched episode id
        List<WatchedEpisode> watched = watchedEpisodeRepository.findByUserIdAndEpisode_Series_Id(userId, seriesId);
        Set<Long> watchedEpisodeIds = watched.stream()
                .map(w -> w.getEpisode().getId())
                .collect(Collectors.toSet());

        // create episodes
        List<Episode> allEpisodes = episodeRepository.findNextEpisodes(seriesId, -1);

        List<SeriesDetailsDto.EpisodeItem> episodeItems = allEpisodes.stream().map(ep -> {
            SeriesDetailsDto.EpisodeItem item = new SeriesDetailsDto.EpisodeItem();
            item.setId(ep.getId());
            item.setSeasonNumber(ep.getSeasonNumber());
            item.setEpisodeNumber(ep.getEpisodeNumber());
            item.setTitle(ep.getTitle());
            item.setWatchable(Boolean.TRUE.equals(ep.getWatchable()));
            item.setWatched(watchedEpisodeIds.contains(ep.getId()));
            return item;
        }).collect(Collectors.toList());

        dto.setEpisodes(episodeItems);
        log.debug(
            "Series details generated, userId={}, seriesId={}, episodes={}",
            userId,
            seriesId,
            episodeItems.size()
        );

        return dto;
    }
}

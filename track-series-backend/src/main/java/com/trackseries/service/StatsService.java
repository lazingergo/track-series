package com.trackseries.service;

import com.trackseries.dto.MonthlyStatsDto;
import com.trackseries.entity.User;
import com.trackseries.entity.WatchedEpisode;
import com.trackseries.exception.ResourceNotFoundException;
import com.trackseries.repository.UserRepository;
import com.trackseries.repository.WatchedEpisodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class StatsService {
    private static final Logger log = LoggerFactory.getLogger(StatsService.class);

    private final UserRepository userRepository;
    private final WatchedEpisodeRepository watchedEpisodeRepository;

    public StatsService(UserRepository userRepository, WatchedEpisodeRepository watchedEpisodeRepository) {
        this.userRepository = userRepository;
        this.watchedEpisodeRepository = watchedEpisodeRepository;
    }

    public MonthlyStatsDto getMonthlyStatsForUsername(String username, Integer requestedYear) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        List<Integer> availableYears = watchedEpisodeRepository.findDistinctWatchedYearsByUserId(user.getId());

        int year = requestedYear != null ? requestedYear : LocalDateTime.now().getYear();
        if (requestedYear == null && !availableYears.isEmpty()) {
            year = availableYears.get(0);
        }

        LocalDateTime from = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime to = from.plusYears(1);

        List<WatchedEpisode> watchedInYear = watchedEpisodeRepository.findByUserIdAndWatchedAtBetween(user.getId(), from, to);

        List<Integer> monthlyCounts = new ArrayList<>(Collections.nCopies(12, 0));
        for (WatchedEpisode watchedEpisode : watchedInYear) {
            int monthIndex = watchedEpisode.getWatchedAt().getMonthValue() - 1;
            monthlyCounts.set(monthIndex, monthlyCounts.get(monthIndex) + 1);
        }

        MonthlyStatsDto dto = new MonthlyStatsDto();
        dto.setYear(year);
        dto.setMonthlyCounts(monthlyCounts);
        dto.setAvailableYears(availableYears);

        log.debug("Monthly stats generated, username='{}', year={}, totalWatchedInYear={}", username, year, watchedInYear.size());
        return dto;
    }
}

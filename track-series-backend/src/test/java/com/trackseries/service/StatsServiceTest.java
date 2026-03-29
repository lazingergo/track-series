package com.trackseries.service;

import com.trackseries.dto.MonthlyStatsDto;
import com.trackseries.entity.User;
import com.trackseries.entity.WatchedEpisode;
import com.trackseries.exception.ResourceNotFoundException;
import com.trackseries.repository.UserRepository;
import com.trackseries.repository.WatchedEpisodeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WatchedEpisodeRepository watchedEpisodeRepository;

    @InjectMocks
    private StatsService statsService;

    @Test
    void getMonthlyStatsForUsername_whenUserMissing_thenThrowsNotFound() {
        when(userRepository.findByUsername("missing-user")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> statsService.getMonthlyStatsForUsername("missing-user", 2026))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("missing-user");
    }

    @Test
    void getMonthlyStatsForUsername_whenYearProvided_thenBuildsMonthlyCounts() {
        User user = new User();
        user.setId(1L);
        user.setUsername("john");

        WatchedEpisode janWatch = new WatchedEpisode();
        janWatch.setWatchedAt(LocalDateTime.of(2026, 1, 10, 12, 0));

        WatchedEpisode marWatch1 = new WatchedEpisode();
        marWatch1.setWatchedAt(LocalDateTime.of(2026, 3, 2, 9, 0));

        WatchedEpisode marWatch2 = new WatchedEpisode();
        marWatch2.setWatchedAt(LocalDateTime.of(2026, 3, 25, 21, 30));

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(watchedEpisodeRepository.findDistinctWatchedYearsByUserId(1L)).thenReturn(List.of(2026, 2025));
        when(watchedEpisodeRepository.findByUserIdAndWatchedAtBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(janWatch, marWatch1, marWatch2));

        MonthlyStatsDto result = statsService.getMonthlyStatsForUsername("john", 2026);

        assertThat(result.getYear()).isEqualTo(2026);
        assertThat(result.getAvailableYears()).containsExactly(2026, 2025);
        assertThat(result.getMonthlyCounts()).hasSize(12);
        assertThat(result.getMonthlyCounts().get(0)).isEqualTo(1); // January
        assertThat(result.getMonthlyCounts().get(2)).isEqualTo(2); // March
        assertThat(result.getMonthlyCounts().stream().mapToInt(Integer::intValue).sum()).isEqualTo(3);
    }
}

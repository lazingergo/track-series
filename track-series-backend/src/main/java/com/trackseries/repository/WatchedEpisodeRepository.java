package com.trackseries.repository;

import com.trackseries.entity.WatchedEpisode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface WatchedEpisodeRepository extends JpaRepository<WatchedEpisode, Long> {
    Optional<WatchedEpisode> findByUserIdAndEpisodeId(
            Long userId, Long episodeId
    );

    @Query("""
            SELECT COUNT(w)
            FROM WatchedEpisode w
            WHERE w.user.id = :userId
              AND w.episode.series.id = :seriesId
              AND w.episode.seasonNumber > :seasonNumber
            """)
    long countNextEpisodes(Long userId, Long seriesId, Integer seasonNumber);

    List<WatchedEpisode> findByUserIdAndEpisode_Series_Id(
            Long userId, Long seriesId
    );

    List<WatchedEpisode> findByUserIdAndEpisode_Series_IdIn(Long userId, List<Long> seriesIds);

    @Query("""
            SELECT w.episode.id
            FROM WatchedEpisode w
            WHERE w.user.id = :userId
              AND w.episode.id IN :episodeIds
            """)
    List<Long> findExistingEpisodeIds(Long userId, List<Long> episodeIds);

    void deleteByUserIdAndEpisode_Series_Id(Long userId, Long seriesId);

    List<WatchedEpisode> findByUserIdAndWatchedAtBetween(Long userId, LocalDateTime from, LocalDateTime to);

    @Query("""
            SELECT DISTINCT YEAR(w.watchedAt)
            FROM WatchedEpisode w
            WHERE w.user.id = :userId
            ORDER BY YEAR(w.watchedAt) DESC
            """)
    List<Integer> findDistinctWatchedYearsByUserId(Long userId);
}

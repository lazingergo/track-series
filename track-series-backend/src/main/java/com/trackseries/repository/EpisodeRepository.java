package com.trackseries.repository;

import com.trackseries.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EpisodeRepository extends JpaRepository<Episode, Long> {
    // get all episode
    List<Episode> findBySeriesId(Long seriesId);

    // counts how many episodes there are in the series after a given season
    long countBySeriesIdAndSeasonNumberGreaterThan(Long seriesId, Integer seasonNumber);

    @Query("""
        SELECT e FROM Episode e
        WHERE e.series.id = :seriesId
         AND e.seasonNumber > :seasonNumber
        ORDER BY e.seasonNumber ASC, e.episodeNumber ASC
        """)
    List<Episode> findNextEpisodes(Long seriesId, Integer seasonNumber);

}

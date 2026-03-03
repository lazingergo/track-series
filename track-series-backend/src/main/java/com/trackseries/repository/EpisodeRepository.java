package com.trackseries.repository;

import com.trackseries.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EpisodeRepository extends JpaRepository<Episode, Long> {
    // get all episode
    List<Episode> findBySeriesId(Long seriesId);

    // counts how many episodes there are in the series after a given season
    long countBySeriesIdAndSeasonNumberGreaterThan(Long seriesId, Integer seasonNumber);
}

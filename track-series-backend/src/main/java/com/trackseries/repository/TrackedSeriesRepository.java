package com.trackseries.repository;

import com.trackseries.entity.TrackedSeries;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrackedSeriesRepository extends JpaRepository<TrackedSeries, Long> {
    @EntityGraph(attributePaths = {"series"})
    List<TrackedSeries> findByUserId(Long userId);

    Optional<TrackedSeries> findByUserIdAndSeriesId(Long userId, Long seriesId);
}

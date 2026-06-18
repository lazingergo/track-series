package com.trackseries.repository;

import com.trackseries.entity.TrackedSeries;
import com.trackseries.enums.WatchStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrackedSeriesRepository extends JpaRepository<TrackedSeries, Long> {
    @EntityGraph(attributePaths = {"series"})
    List<TrackedSeries> findByUserId(Long userId);

    Optional<TrackedSeries> findByUserIdAndSeriesId(Long userId, Long seriesId);

        @Query("""
        select distinct t.series.id
        from TrackedSeries t
        where t.user.id = :userId
            and t.status in :statuses
        """)
        List<Long> findDistinctSeriesIdsByUserIdAndStatusIn(
                @Param("userId") Long userId,
                @Param("statuses") List<WatchStatus> statuses
        );

    @Query("""
    select distinct t.series.id
    from TrackedSeries t
    where t.status in :statuses
    """)
    List<Long> findDistinctSeriesIdsByStatusIn(@Param("statuses") List<WatchStatus> statuses);
}

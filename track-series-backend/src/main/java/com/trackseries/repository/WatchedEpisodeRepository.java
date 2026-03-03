package com.trackseries.repository;

import com.trackseries.entity.WatchedEpisode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
public interface WatchedEpisodeRepository extends JpaRepository<WatchedEpisode,Long> {
    Optional<WatchedEpisode> findByUserIdAndEpisodeId(Long userId, Long episodeId);
}

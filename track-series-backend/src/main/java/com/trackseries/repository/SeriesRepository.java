package com.trackseries.repository;

import com.trackseries.entity.Series;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeriesRepository extends JpaRepository<Series,Long> {

}

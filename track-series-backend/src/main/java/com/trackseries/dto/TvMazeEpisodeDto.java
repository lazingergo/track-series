package com.trackseries.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDate;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TvMazeEpisodeDto {
    private Long id;
    private String name;
    private Integer season;
    private Integer number;
    private LocalDate airdate;
    private String summary;
}

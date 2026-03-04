package com.trackseries.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TvMazeSeriesDto {
    private Long id;
    private String name;
    private String summary;
    private String status;
    private LocalDate premiered;
    private LocalDate ended;
    private List<String> genres;
    private TvMazaImageDto image;

    @JsonAlias("_embedded")
    private Embedded embedded;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Embedded {
        private List<TvMazeEpisodeDto> episodes;
    }
}

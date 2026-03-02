package com.trackseries.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.Data;
import org.springframework.cglib.core.Local;

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
    private Local ended;
    private List<String> genres;
    private TvMazaImageDto image;

    @JsonAlias("_embedded")
    private Embedded _embedded;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Embedded {
        private List<TvMazeEpisodeDto> episode;
    }
}

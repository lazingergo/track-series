package com.trackseries.dto;

import lombok.Data;

@Data
public class SeriesSearchResultDto {
    private Long tvMazeId;
    private String title;
    private String imageUrl;
    private String releaseDate;
}
package com.trackseries.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpNextDto {
    private List<NextEpisodeItem> watching;
    private List<NextEpisodeItem> planToWatch;

    @Data
    public static class NextEpisodeItem {
        private Long seriesId;
        private String seriesTitle;
        private String imageUrl;

        // next episode data
        private Long nextEpisodeId;
        private Integer seasonNumber;
        private Integer episodeNumber;
        private String episodeTitle;
    }
}

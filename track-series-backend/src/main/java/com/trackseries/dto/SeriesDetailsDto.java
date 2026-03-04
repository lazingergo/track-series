package com.trackseries.dto;

import com.trackseries.enums.WatchStatus;
import lombok.Data;

import java.util.List;

@Data
public class SeriesDetailsDto {
    private Long id;
    private String title;
    private String summary;
    private String imageUrl;

    private WatchStatus userStatus;
    private Integer userRating;

    private List<EpisodeItem> episodes;

    @Data
    public static class EpisodeItem {
        private Long id;
        private Integer seasonNumber;
        private Integer episodeNumber;
        private String title;
        private boolean isWatched;
    }
}

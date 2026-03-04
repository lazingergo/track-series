package com.trackseries.dto;

import com.trackseries.enums.WatchStatus;
import lombok.Data;

import java.util.List;

@Data
public class ProfileDto {
    private String username;
    private List<TrackedSeriesItem> series;

    @Data
    public static class TrackedSeriesItem {
        private Long seriesId;
        private String title;
        private String imageUrl;
        private WatchStatus status;
    }
}
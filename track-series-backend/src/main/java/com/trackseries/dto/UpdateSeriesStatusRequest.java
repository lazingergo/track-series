package com.trackseries.dto;

import com.trackseries.enums.WatchStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateSeriesStatusRequest {
    @NotNull(message = "status is required")
    private WatchStatus status;
}

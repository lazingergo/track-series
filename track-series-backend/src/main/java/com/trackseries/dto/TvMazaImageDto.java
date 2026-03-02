package com.trackseries.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.trackseries.entity.Series;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TvMazaImageDto {
    private String medium;
    private String original;
}

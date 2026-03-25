package com.trackseries.dto;

import lombok.Data;

import java.util.List;

@Data
public class MonthlyStatsDto {
    private int year;
    private List<Integer> monthlyCounts;
    private List<Integer> availableYears;
}

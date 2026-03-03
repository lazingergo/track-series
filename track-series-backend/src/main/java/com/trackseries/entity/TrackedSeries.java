package com.trackseries.entity;

import com.trackseries.enums.WatchStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tracked_series")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrackedSeries {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id", nullable = false)
    private Series series;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WatchStatus status = WatchStatus.PLAN_TO_WATCH;

    private Integer rating;
}


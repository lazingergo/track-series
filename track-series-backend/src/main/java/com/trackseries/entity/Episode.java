package com.trackseries.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "episode")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Episode {

    @Id
    private Long id;

    private String title;

    private Integer seasonNumber;

    private Integer episodeNumber;

    private LocalDate airdate;

    @Column(columnDefinition = "TEXT")
    private String summary;
    public boolean isWatchable() {
        return airdate != null && !airdate.isAfter(LocalDate.now());
    }
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id")
    @JsonIgnore
    private Series series;

    @OneToMany(mappedBy = "episode", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<WatchedEpisode> watchedByUsers = new ArrayList<>();

}

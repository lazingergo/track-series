package com.trackseries.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "series")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Series {
    @Id
    private Long id; // TV maze id

    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

    private String imageUrl;

    private String status; // "Running", "Ended"

    private LocalDate premiered;

    private LocalDate ended;

    @OneToMany(mappedBy = "series", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Episode> episodes = new ArrayList<>();

}

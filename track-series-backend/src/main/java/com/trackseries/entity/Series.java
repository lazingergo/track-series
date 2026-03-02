package com.trackseries.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

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

}

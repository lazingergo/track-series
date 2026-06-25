package com.trackseries.entity;

import com.trackseries.enums.NotificationStatus;
import com.trackseries.enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "series_id", nullable = false)
    private Series series;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime created_at;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduled_at;

    @Column(name = "sent_at")
    private LocalDateTime sent_at;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private NotificationStatus status;

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Column(name = "html_body", nullable = false, columnDefinition = "LONGTEXT")
    private String html_body;

    @Column(name = "text_body", columnDefinition = "LONGTEXT")
    private String text_body;

    @Column(name = "retry_count", nullable = false)
    private Integer retry_count = 0;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String last_error;
}

package com.trackseries.service;

import com.trackseries.dto.ProfileDto;
import com.trackseries.entity.TrackedSeries;
import com.trackseries.entity.User;
import com.trackseries.repository.TrackedSeriesRepository;
import com.trackseries.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileService {
    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);

    private final UserRepository userRepository;
    private final TrackedSeriesRepository trackedSeriesRepository;

    public ProfileService(UserRepository userRepository, TrackedSeriesRepository trackedSeriesRepository) {
        this.userRepository = userRepository;
        this.trackedSeriesRepository = trackedSeriesRepository;
    }

    public ProfileDto getProfileByUsername(String username) {
        log.debug("Profile requested for username='{}'", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User cannot find with this username " + username));

        List<TrackedSeries> trackedSeries = trackedSeriesRepository.findByUserId(user.getId());

        ProfileDto dto = new ProfileDto();
        dto.setUsername(user.getUsername());
        dto.setSeries(trackedSeries.stream().map(item -> {
            ProfileDto.TrackedSeriesItem card = new ProfileDto.TrackedSeriesItem();
            card.setSeriesId(item.getSeries().getId());
            card.setTitle(item.getSeries().getTitle());
            card.setImageUrl(item.getSeries().getImageUrl());
            card.setStatus(item.getStatus());
            return card;
        }).toList());

        log.debug("Profile built for username='{}', trackedSeriesCount={}", username, dto.getSeries().size());

        return dto;
    }
}
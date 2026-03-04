package com.trackseries.service;

import com.trackseries.dto.ProfileDto;
import com.trackseries.entity.TrackedSeries;
import com.trackseries.entity.User;
import com.trackseries.repository.TrackedSeriesRepository;
import com.trackseries.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final TrackedSeriesRepository trackedSeriesRepository;

    public ProfileService(UserRepository userRepository, TrackedSeriesRepository trackedSeriesRepository) {
        this.userRepository = userRepository;
        this.trackedSeriesRepository = trackedSeriesRepository;
    }

    public ProfileDto getProfileByUsername(String username) {
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

        return dto;
    }
}
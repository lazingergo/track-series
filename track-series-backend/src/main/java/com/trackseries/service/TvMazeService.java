package com.trackseries.service;

import com.trackseries.dto.TvMazeSeriesDto;
import com.trackseries.entity.Episode;
import com.trackseries.entity.Genre;
import com.trackseries.entity.Series;
import com.trackseries.repository.EpisodeRepository;
import com.trackseries.repository.GenreRepository;
import com.trackseries.repository.SeriesRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class TvMazeService {

    private final SeriesRepository seriesRepository;
    private final GenreRepository genreRepository;
    private final RestClient restClient;
    public TvMazeService(SeriesRepository seriesRepository,
                         GenreRepository genreRepository,
                         @Value("${tvmaze.api.base-url}") String baseUrl) {

        this.seriesRepository = seriesRepository;
        this.genreRepository = genreRepository;
        this.restClient = RestClient.create(baseUrl);
    }

    @Transactional
    public Series fetchAndSaveSeries(Long tvMazeId) {
        if (seriesRepository.existsById(tvMazeId)) {
            System.out.println("The series is already in the database! " + tvMazeId);
            return seriesRepository.findById(tvMazeId).orElseThrow();
        }

        System.out.println("Download series from TVmaze");

        TvMazeSeriesDto dto = restClient.get()
                .uri("/shows/{id}?embed=episodes",tvMazeId)
                .retrieve()
                .body(TvMazeSeriesDto.class);

        if (dto == null) {
            throw new RuntimeException("No sereis fond with this ID" + tvMazeId);
        }

        // Mapping
        Series series = new Series();
        series.setId(dto.getId());
        series.setTitle(dto.getName());
        series.setSummary(dto.getSummary());
        series.setStatus(dto.getStatus());
        series.setPremiered(dto.getPremiered());
        series.setEnded(dto.getEnded());

        if (dto.getImage() != null) {
            series.setImageUrl(dto.getImage().getOriginal() != null ? dto.getImage().getOriginal() : dto.getImage().getMedium());
        }

        // genres
        if (dto.getGenres() != null) {
            for(String genreName : dto.getGenres()) {
                // check if the genre is already exist in the database
                Genre genre = genreRepository.findByName(genreName)
                        .orElseGet(() -> {
                            Genre newGenre = new Genre();
                            newGenre.setName(genreName);
                            return genreRepository.save(newGenre);
                        });
                series.getGenres().add(genre);
            }
        }

        // episodes
        if (dto.getEmbedded() != null && dto.getEmbedded().getEpisodes() != null) {
            for (var epDto : dto.getEmbedded().getEpisodes()) {
                Episode episode = new Episode();
                episode.setId(epDto.getId());
                episode.setTitle(epDto.getName());
                episode.setSeasonNumber(epDto.getSeason());
                episode.setEpisodeNumber(epDto.getNumber());
                episode.setAirdate(epDto.getAirdate());
                episode.setSummary(epDto.getSummary());

                // set the episode for the series, and the series for the episode.
                episode.setSeries(series);
                series.getEpisodes().add(episode);
            }
        }

        return seriesRepository.save(series);

    }

}

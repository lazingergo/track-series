package com.trackseries.service;

import com.trackseries.dto.SeriesSearchResultDto;
import com.trackseries.dto.TvMazeSeriesDto;
import com.trackseries.entity.*;
import com.trackseries.exception.ResourceNotFoundException;
import com.trackseries.repository.EpisodeRepository;
import com.trackseries.repository.GenreRepository;
import com.trackseries.repository.SeriesRepository;
import com.trackseries.repository.TrackedSeriesRepository;
import com.trackseries.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TvMazeService {
    private static final Logger log = LoggerFactory.getLogger(TvMazeService.class);

    private final SeriesRepository seriesRepository;
    private final EpisodeRepository episodeRepository;
    private final GenreRepository genreRepository;
    private final RestClient restClient;
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final TrackedSeriesRepository trackedSeriesRepository;

    public TvMazeService(SeriesRepository seriesRepository,
                         EpisodeRepository episodeRepository,
                         GenreRepository genreRepository,
                         @Value("${tvmaze.api.base-url}") String baseUrl,
                         UserRepository userRepository,
                         TrackedSeriesRepository trackedSeriesRepository) {

        this.seriesRepository = seriesRepository;
        this.episodeRepository = episodeRepository;
        this.genreRepository = genreRepository;
        this.restClient = RestClient.create(baseUrl);
        this.restTemplate = new RestTemplate();
        this.userRepository = userRepository;
        this.trackedSeriesRepository = trackedSeriesRepository;
    }

    @Transactional
    public Series fetchAndSaveSeries(Long tvMazeId) {
        if (seriesRepository.existsById(tvMazeId)) {
            log.info("Series already exists in DB, tvMazeId={}", tvMazeId);
            return seriesRepository.findById(tvMazeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Series not found with id " + tvMazeId));
        }

        log.info("Importing series from TVMaze, tvMazeId={}", tvMazeId);

        TvMazeSeriesDto dto = restClient.get()
                .uri("/shows/{id}?embed=episodes", tvMazeId)
                .retrieve()
                .body(TvMazeSeriesDto.class);

        if (dto == null) {
            log.warn("No TVMaze series found for id={}", tvMazeId);
            throw new ResourceNotFoundException("No series found with id " + tvMazeId);
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
            series.setImageUrl(
                    dto.getImage().getOriginal() != null
                            ? dto.getImage().getOriginal()
                            : dto.getImage().getMedium()
            );
        }

        // genres
        if (dto.getGenres() != null) {
            for (String genreName : dto.getGenres()) {
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
                episode.setWatchable(isWatchable(epDto.getAirdate()));
                // set the episode for the series, and the series for the episode.
                episode.setSeries(series);
                series.getEpisodes().add(episode);
            }
        }

        Series saved = seriesRepository.save(series);
        log.info(
            "Series imported successfully, tvMazeId={}, episodesCount={}, genresCount={}",
            tvMazeId,
            saved.getEpisodes().size(),
            saved.getGenres().size()
        );
        return saved;

    }

    @Transactional
    public int refreshSeriesEpisodes(Long tvMazeId) {
        Series series = seriesRepository.findById(tvMazeId)
            .orElseThrow(() -> new ResourceNotFoundException("Series not found with id " + tvMazeId));

        TvMazeSeriesDto dto = restClient.get()
            .uri("/shows/{id}?embed=episodes", tvMazeId)
            .retrieve()
            .body(TvMazeSeriesDto.class);

        if (dto == null) {
            throw new ResourceNotFoundException("No series found with id " + tvMazeId);
        }

        series.setTitle(dto.getName());
        series.setSummary(dto.getSummary());
        series.setStatus(dto.getStatus());
        series.setPremiered(dto.getPremiered());
        series.setEnded(dto.getEnded());

        if (dto.getImage() != null) {
            series.setImageUrl(
                dto.getImage().getOriginal() != null
                    ? dto.getImage().getOriginal()
                    : dto.getImage().getMedium()
            );
        }

        Set<Long> existingEpisodeIds = series.getEpisodes().stream()
            .map(Episode::getId)
            .collect(Collectors.toCollection(HashSet::new));

        int addedEpisodes = 0;
        if (dto.getEmbedded() != null && dto.getEmbedded().getEpisodes() != null) {
            for (var epDto : dto.getEmbedded().getEpisodes()) {
                if (existingEpisodeIds.contains(epDto.getId()) || episodeRepository.existsById(epDto.getId())) {
                    continue;
                }

                Episode episode = new Episode();
                episode.setId(epDto.getId());
                episode.setTitle(epDto.getName());
                episode.setSeasonNumber(epDto.getSeason());
                episode.setEpisodeNumber(epDto.getNumber());
                episode.setAirdate(epDto.getAirdate());
                episode.setSummary(epDto.getSummary());
                episode.setSeries(series);
                episode.setWatchable(isWatchable(epDto.getAirdate()));
                series.getEpisodes().add(episode);
                addedEpisodes++;
            }
        }

        seriesRepository.save(series);
        log.info("Series refreshed, tvMazeId={}, addedEpisodes={}", tvMazeId, addedEpisodes);
        return addedEpisodes;
    }

    private boolean isWatchable(LocalDate airdate) {
        return airdate != null && !airdate.isAfter(LocalDate.now());
    }

    public List<SeriesSearchResultDto> searchShows(String query, String username) {
        log.debug("Searching TVMaze shows with query='{}'", query);
        String url = "https://api.tvmaze.com/search/shows?q=" + query;

        // get JSON from TVMaze
        JsonNode rootNode = restTemplate.getForObject(url, JsonNode.class);
        List<SeriesSearchResultDto> results = new ArrayList<>();

        // get user-added series
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User cannot find with this username " + username));

        List<TrackedSeries> trackedSeries = trackedSeriesRepository.findByUserId(user.getId());
        Set<Long> trackedSeriesIds = trackedSeries.stream()
            .map(item -> item.getSeries().getId())
            .collect(Collectors.toSet());

        if (rootNode != null && rootNode.isArray()) {
            for (JsonNode node : rootNode) {
                JsonNode show = node.path("show");

                SeriesSearchResultDto dto = new SeriesSearchResultDto();
                dto.setTvMazeId(show.path("id").asLong());
                JsonNode nameNode = show.path("name");
                dto.setTitle(nameNode.isTextual() ? nameNode.textValue() : "");
                dto.setAlreadyAdded(trackedSeriesIds.contains(dto.getTvMazeId()));

                // premier date
                if (!show.path("premiered").isNull() && !show.path("premiered").isMissingNode()) {
                    JsonNode premieredNode = show.path("premiered");
                    dto.setReleaseDate(premieredNode.isTextual() ? premieredNode.textValue() : null);
                }

                // poster
                JsonNode image = show.path("image");
                if (!image.isNull() && !image.isMissingNode()) {
                    JsonNode mediumNode = image.path("medium");
                    dto.setImageUrl(mediumNode.isTextual() ? mediumNode.textValue() : null);
                }

                results.add(dto);
            }
        }
        log.debug("TVMaze search completed, query='{}', results={}", query, results.size());
        return results;
    }

}

package com.trackseries.service;

import com.trackseries.dto.SeriesSearchResultDto;
import com.trackseries.dto.TvMazeSeriesDto;
import com.trackseries.entity.Episode;
import com.trackseries.entity.Genre;
import com.trackseries.entity.Series;
import com.trackseries.repository.GenreRepository;
import com.trackseries.repository.SeriesRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

@Service
public class TvMazeService {

    private final SeriesRepository seriesRepository;
    private final GenreRepository genreRepository;
    private final RestClient restClient;
    private final RestTemplate restTemplate;

    public TvMazeService(SeriesRepository seriesRepository,
                         GenreRepository genreRepository,
                         @Value("${tvmaze.api.base-url}") String baseUrl) {

        this.seriesRepository = seriesRepository;
        this.genreRepository = genreRepository;
        this.restClient = RestClient.create(baseUrl);
        this.restTemplate = new RestTemplate();
    }

    @Transactional
    public Series fetchAndSaveSeries(Long tvMazeId) {
        if (seriesRepository.existsById(tvMazeId)) {
            System.out.println("The series is already in the database! " + tvMazeId);
            return seriesRepository.findById(tvMazeId).orElseThrow();
        }

        System.out.println("Download series from TVmaze");

        TvMazeSeriesDto dto = restClient.get()
                .uri("/shows/{id}?embed=episodes", tvMazeId)
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

                // set the episode for the series, and the series for the episode.
                episode.setSeries(series);
                series.getEpisodes().add(episode);
            }
        }

        return seriesRepository.save(series);

    }

    public List<SeriesSearchResultDto> searchShows(String query) {
        String url = "https://api.tvmaze.com/search/shows?q=" + query;

        // Lekérjük a nyers JSON-t a TVMaze-től
        JsonNode rootNode = restTemplate.getForObject(url, JsonNode.class);
        List<SeriesSearchResultDto> results = new ArrayList<>();

        if (rootNode != null && rootNode.isArray()) {
            for (JsonNode node : rootNode) {
                JsonNode show = node.path("show"); // A TVMaze a 'show' objektumba rejti a lényeget

                SeriesSearchResultDto dto = new SeriesSearchResultDto();
                dto.setTvMazeId(show.path("id").asLong());
                dto.setTitle(show.path("name").asText());

                // Premier dátum (lehet null is, ha még nem jelent meg)
                if (!show.path("premiered").isNull() && !show.path("premiered").isMissingNode()) {
                    dto.setReleaseDate(show.path("premiered").asText());
                }

                // Kép (szintén lehet null, ha nincs hozzá plakát)
                JsonNode image = show.path("image");
                if (!image.isNull() && !image.isMissingNode()) {
                    dto.setImageUrl(image.path("medium").asText());
                }

                results.add(dto);
            }
        }
        return results;
    }

}

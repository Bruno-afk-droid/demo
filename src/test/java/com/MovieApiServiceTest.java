package com;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.model.OmdbMovieResponse;
import com.model.TmdbMovieResponse;

@ExtendWith(MockitoExtension.class)
public class MovieApiServiceTest {


    @Mock
    private org.springframework.web.client.RestTemplate restTemplate;

    @InjectMocks
    private com.service.MovieApiService movieApiService;

    @Test
    void testGetMovieDetailsOmdb_Success() throws Exception {
        // Arrange
        OmdbMovieResponse omdbResponse = new OmdbMovieResponse();
        omdbResponse.setTitle("Test Movie");
        omdbResponse.setYear("2020");
        String expectedUrl = "http://www.omdbapi.com/?t=Test+Movie&apikey=42138832";
        when(restTemplate.getForObject(expectedUrl, OmdbMovieResponse.class)).thenReturn(omdbResponse);

        // Act
        CompletableFuture<OmdbMovieResponse> result = movieApiService.getMovieDetails_omd("Test Movie");

        // Assert
        assertNotNull(result.get());
        assertEquals("Test Movie", result.get().getTitle());
        assertEquals("2020", result.get().getYear());
    }

    @Test
    void testGetMovieDetailsOmdb_Failure() throws Exception {
        // Arrange
        String expectedUrl = "http://www.omdbapi.com/?t=Test+Movie&apikey=42138832";
        when(restTemplate.getForObject(expectedUrl, OmdbMovieResponse.class)).thenThrow(new RuntimeException("Connection failed"));

        // Act
        CompletableFuture<OmdbMovieResponse> result = movieApiService.getMovieDetails_omd("Test Movie");

        // Assert
        assertNull(result.get());
    }
    @Test
    void testGetMovieDetailsTmdb_Success() throws Exception {
        // Arrange
        TmdbMovieResponse tmdbResponse = new TmdbMovieResponse();
        String expectedUrl = "https://api.themoviedb.org/3/search/movie?query=Test+Movie&api_key=2af894f45496ead36ca797704fb707b0";
        when(restTemplate.getForObject(expectedUrl, TmdbMovieResponse.class)).thenReturn(tmdbResponse);

        // Act
        CompletableFuture<TmdbMovieResponse> result = movieApiService.getMovieDetails_tmdb("Test Movie");

        // Assert
        assertNotNull(result.get());
        assertEquals(tmdbResponse, result.get());
    }

    @Test
    void testGetMovieDetailsTmdb_Failure() throws Exception {
        // Arrange
        String expectedUrl = "https://api.themoviedb.org/3/search/movie?query=Test+Movie&api_key=2af894f45496ead36ca797704fb707b0";
        when(restTemplate.getForObject(expectedUrl, TmdbMovieResponse.class)).thenThrow(new RuntimeException("Connection failed"));

        // Act
        CompletableFuture<TmdbMovieResponse> result = movieApiService.getMovieDetails_tmdb("Test Movie");

        // Assert
        assertNull(result.get());
    }

    @Test
    void testGetMovieImagesTmdb_Success() throws Exception {
        // Arrange
        int movieId = 123;
        String expectedUrl = "https://api.themoviedb.org/3/movie/123/images?api_key=2af894f45496ead36ca797704fb707b0";
        String imageJson = "{\"backdrops\":[]}";
        when(restTemplate.getForObject(expectedUrl, String.class)).thenReturn(imageJson);

        // Act
        CompletableFuture<String> result = movieApiService.getMovieImages_tmdb(movieId);

        // Assert
        assertNotNull(result.get());
        assertEquals(imageJson, result.get());
    }

    @Test
    void testGetMovieImagesTmdb_Failure() throws Exception {
        // Arrange
        int movieId = 123;
        String expectedUrl = "https://api.themoviedb.org/3/movie/123/images?api_key=2af894f45496ead36ca797704fb707b0";
        when(restTemplate.getForObject(expectedUrl, String.class)).thenThrow(new RuntimeException("Connection failed"));

        // Act
        CompletableFuture<String> result = movieApiService.getMovieImages_tmdb(movieId);

        // Assert
        assertNull(result.get());
    }
}
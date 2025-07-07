
package com;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.model.Movie;
import com.repository.MovieRepository;
import com.service.MovieService;

@ExtendWith(MockitoExtension.class)
public class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository; // your own type

    @InjectMocks
    private MovieService movieService; // the class under test

    @Test
    void testFindMovie() {
        // Arrange
        Movie movie = new Movie();
        movie.setTitle("Inception");
        when(movieRepository.findByTitle("Inception")).thenReturn(Optional.of(movie));

        // Act
        Optional<Movie> result = movieService.findMovieByTitle("Inception");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Inception", result.get().getTitle());
    }

    @Test
    void testUpdateMovie() {
        // Arrange
        String title = "Inception";
        boolean watched = true;
        int rating = 5;
        // Suppose updateMovie returns true when update is successful
        // You own MovieRepository, but updateMovie delegates to MovieDBManager, so you would mock that if needed
        // For this test, let's assume you want to mock the repository method existsByTitle and test the service logic
        // But MovieService.updateMovie delegates to movieDBManager.updateMovie, so you would need to mock that if you want to test updateMovie
        // If you want to test the repository-based update, you could add such a method to MovieService and test it here
        // For now, let's assume you want to test the DBManager-based update
        com.repository.MovieDBManager movieDBManager = org.mockito.Mockito.mock(com.repository.MovieDBManager.class);
        MovieService movieServiceWithMockedDB = new MovieService();
        java.lang.reflect.Field dbField;
        try {
            dbField = MovieService.class.getDeclaredField("movieDBManager");
            dbField.setAccessible(true);
            dbField.set(movieServiceWithMockedDB, movieDBManager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        org.mockito.Mockito.when(movieDBManager.updateMovie(title, watched, rating)).thenReturn(true);

        // Act
        boolean result = movieServiceWithMockedDB.updateMovie(title, watched, rating);

        // Assert
        assertTrue(result);
    }

    @Test
    void testInsertMovie() {
        // Arrange
        Movie movie = new Movie();
        movie.setTitle("Interstellar");
        when(movieRepository.save(movie)).thenReturn(movie);

        // Act
        Movie result = movieService.insertMovie(movie);

        // Assert
        assertEquals("Interstellar", result.getTitle());
    }

    @Test
    void testDeleteMovie() {
        // Arrange
        String title = "Inception";
        when(movieRepository.existsByTitle(title)).thenReturn(true);
        // No need to stub deleteByTitle since it's void, but you can verify it was called

        // Act
        boolean result = movieService.deleteMovie(title);

        // Assert
        assertTrue(result);
        org.mockito.Mockito.verify(movieRepository).deleteByTitle(title);
    }
}
package com.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.model.Movie;
import com.repository.MovieDBManager;
import com.repository.MovieRepository;

// Service class that acts as an intermediary between controllers and the MovieDBManager (database layer)
@Service
public class MovieService {
    // Inject the MovieDBManager for database operations
    @Autowired
    private MovieDBManager movieDBManager;

    @Autowired
    private MovieRepository movieRepository;

    /**
     * Retrieves a paginated list of movie titles.
     * @param page The page number (1-based)
     * @param pageSize The number of movies per page
     * @return List of movie titles for the requested page
     */
    public List<String> getMovies(int page, int pageSize){
        return movieDBManager.retrieveMovies(page, pageSize);
    }

    /**
     * Inserts a new movie into the database.
     * @param title Movie title
     * @param year Release year
     * @param director Director name
     * @param genre Movie genre
     * @param similarMovies JSON string of similar movies
     * @param imagePaths JSON string of image paths
     * @param watched Whether the movie has been watched
     * @param rating Movie rating
     * @return true if insertion was successful, false otherwise
     */
    public boolean insertMovie(String title, int year, String director, String genre, String similarMovies, String imagePaths, boolean watched, int rating) {
        return movieDBManager.insertMovie(title, year, director, genre, similarMovies, imagePaths, watched, rating);
    }

    /**
     * Updates the watched status and rating of a movie by title.
     * @param title Movie title
     * @param watched New watched status
     * @param rating New rating
     * @return true if update was successful, false otherwise
     */
    public boolean updateMovie(String title, boolean watched, int rating) {
        return movieDBManager.updateMovie(title, watched, rating);
    }

    /**
     * Deletes a movie from the database by title.
     * @param title Movie title
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteMovie(String title) {
        if (movieRepository.existsByTitle(title)) {
            movieRepository.deleteByTitle(title);
            return true;
        }
        return false;
    }

    public boolean isMovieInDatabase(String title) {
        return movieRepository.existsByTitle(title);
    }

    public Optional<Movie> findMovieByTitle(String title) {
        return movieRepository.findByTitle(title);
    }

    public Movie insertMovie(Movie movie) {
        return movieRepository.save(movie);
    }
}
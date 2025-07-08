package com.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.service.MovieService;
import com.service.RunFile;

@RestController
@RequestMapping("/movies")
public class MovieController {

    @Autowired
    private MovieService movieService;

    @Autowired
    private RunFile runFile;

    // Insert a new movie directly via POST
    /*@org.springframework.web.bind.annotation.PostMapping
    public boolean insertMovie(@org.springframework.web.bind.annotation.RequestBody com.model.Movie movie) {
        return movieService.insertMovieDB(movie);
    }*/

    @org.springframework.web.bind.annotation.PostMapping("/{title}")
    public boolean insertMovieByTitle(@PathVariable String title) {
        
        return runFile.handleMovieDownload(title);
    }

    @GetMapping
    public List<String> getPaginatedMovies(@RequestParam(defaultValue = "1") int page, 
                                          @RequestParam(defaultValue = "5") int size) {
        return movieService.getMovies(page, size);
    }

    // Update an existing movie (by title in path, fields in body)
    @PutMapping("/{title}")
    public Boolean updateMovie(@PathVariable String title, @RequestBody com.model.Movie movie) {
        boolean success = movieService.updateMovie(
            title,
            movie.isWatched(),
            movie.getRating()
        );
        return success;
    }

    // Delete a movie by title in path (for RESTful style)
    @DeleteMapping("/{title}")
    public boolean deleteMovieByTitle(@PathVariable String title) {
        return movieService.deleteMovie(title);
    }

    // Delete a movie by request body (for flexibility)
    @DeleteMapping
    public boolean deleteMovie(@RequestBody com.model.Movie movie) {
        return movieService.deleteMovie(movie.getTitle());
    }
}

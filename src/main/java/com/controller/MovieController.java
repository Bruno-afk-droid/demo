package com.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import com.service.MovieService;

@RestController
@RequestMapping("/movies")
public class MovieController {

    @Autowired
    private MovieService movieService;

    @GetMapping
    public List<String> getPaginatedMovies(@RequestParam(defaultValue = "1") int page, 
                                          @RequestParam(defaultValue = "5") int size) {
        return movieService.getMovies(page, size);
    }
}

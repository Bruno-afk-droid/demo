package com.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.model.MovieDBManager;

@Service
public class MovieService {
    
    @Autowired
    private MovieDBManager movieDBManager;

    //public Page<Movie> getMovies(int page, int size) {
        //return movieRepository.findAll(PageRequest.of(page, size));

    //}
    public List<String> getMovies(int page, int pageSize){
        return movieDBManager.retrieveMovies(page, pageSize);
    }
}
package com.service;

import com.model.OmdbMovieResponse;
import com.model.TmdbMovieResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.concurrent.CompletableFuture;



@Service
public class MovieApiService {
        // Asynchronously fetch movie details from OMDB
    private static final String API_KEY_omd = "42138832"; // Replace with your OMDB API key
    private static final String BASE_URL_omd = "http://www.omdbapi.com/";
    private static final String BASE_URL_tmdb = "https://api.themoviedb.org/3/";
    private static final String API_KEY_tmdb = "2af894f45496ead36ca797704fb707b0";

    @Autowired
    private RestTemplate restTemplate;

    @Async("taskExecutor")
    public CompletableFuture<OmdbMovieResponse> 
    getMovieDetails_omd(String title) {
        try {
            String url = BASE_URL_omd + "?t=" + title.replace(" ", "+") + "&apikey=" + API_KEY_omd;
            OmdbMovieResponse response = restTemplate.getForObject(url, OmdbMovieResponse.class);
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(null);
        }
    }

    // Asynchronously fetch movie details from TMDB
    @Async("taskExecutor")
    public CompletableFuture<TmdbMovieResponse> getMovieDetails_tmdb(String title) {
        try {
            String url = BASE_URL_tmdb + "search/movie?query=" + title.replace(" ", "+") + "&api_key=" + API_KEY_tmdb;
            TmdbMovieResponse response = restTemplate.getForObject(url, TmdbMovieResponse.class);
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(null);
        }
    }

    // Asynchronously fetch movie images from TMDB
    @Async("taskExecutor")
    public CompletableFuture<String> getMovieImages_tmdb(int id) {
        try {
            String url = BASE_URL_tmdb + "movie/" + id + "/images?api_key=" + API_KEY_tmdb;
            String response = restTemplate.getForObject(url, String.class);
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(null);
        }
    }
}



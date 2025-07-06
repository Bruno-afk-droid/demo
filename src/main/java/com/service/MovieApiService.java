package com.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;



@Service
public class MovieApiService {
        // Asynchronously fetch movie details from OMDB
    private static final String API_KEY_omd = "42138832"; // Replace with your OMDB API key
    private static final String BASE_URL_omd = "http://www.omdbapi.com/";
    private static final String BASE_URL_tmdb = "https://api.themoviedb.org/3/";
    private static final String API_KEY_tmdb = "2af894f45496ead36ca797704fb707b0";

    @Async("taskExecutor")
    public CompletableFuture<String> getMovieDetails_omd(String title) {
        try {
            String urlString = BASE_URL_omd + "?t=" + title.replace(" ", "+") + "&apikey=" + API_KEY_omd;
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            System.out.println("Starting OMDB API call for: " + title + " on thread: " + Thread.currentThread().getName() + " at " + System.currentTimeMillis());
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            System.out.println("Completed OMDB API call for: " + title + " on thread: " + Thread.currentThread().getName() + " at " + System.currentTimeMillis());

            return CompletableFuture.completedFuture(response.toString());
            } catch (Exception e) {
                e.printStackTrace();
                return CompletableFuture.completedFuture(null);
            }
        }

    // Asynchronously fetch movie details from TMDB
    @Async("taskExecutor")
    public CompletableFuture<String> getMovieDetails_tmdb(String title) {
        try {
            String urlString = BASE_URL_tmdb + "search/movie?query=" + title.replace(" ", "+") + "&api_key=" + API_KEY_tmdb;
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            System.out.println("Starting TMDB API call for: " + title + " on thread: " + Thread.currentThread().getName() + " at " + System.currentTimeMillis());
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            System.out.println("Completed TMDB API call for: " + title + " on thread: " + Thread.currentThread().getName() + " at " + System.currentTimeMillis());

            return CompletableFuture.completedFuture(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(null);
        }
    }

    // Asynchronously fetch movie images from TMDB by movie ID
    @Async("taskExecutor")
    public CompletableFuture<String> getMovieImages_tmdb(int id) {
        try {
            String urlString = BASE_URL_tmdb + "movie/" + id + "/images?api_key=" + API_KEY_tmdb;
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            System.out.println("Starting TMDB image API call for ID: " + id + " on thread: " + Thread.currentThread().getName() + " at " + System.currentTimeMillis());
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            System.out.println("Completed TMDB image API call for ID: " + id + " on thread: " + Thread.currentThread().getName() + " at " + System.currentTimeMillis());
                return CompletableFuture.completedFuture(response.toString());
            } catch (Exception e) {
                e.printStackTrace();
                return CompletableFuture.completedFuture(null);
            }
    }
}



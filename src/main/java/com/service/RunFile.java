package com.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.model.Movie;
import com.repository.MovieDBManager;

@Service
public class RunFile {
    // Inject the MovieApiService for API calls
    @Autowired
    private MovieApiService movieApiService;

    // Inject the AsyncImageDownloader for downloading images
    @Autowired
    private AsyncImageDownloader asyncImageDownloader;

    // Inject the MovieDBManager for database operations (lazily to avoid circular dependencies)
    @Autowired
    @Lazy
    private MovieDBManager dbManager;

    // Inject the MovieService for movie entity operations
    @Autowired
    private MovieService movieService;

    public void run() throws Exception {
        // Prompt user for movie title
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the movie title to download: ");
        String movieTitle = scanner.nextLine().trim();

        // Check if movie exists in the database
        if (!dbManager.isMovieInDatabase(movieTitle)) {
            // Execute OMDB and TMDB API calls in parallel using CompletableFuture
            System.out.println("Scheduling OMDB API call at: " + System.currentTimeMillis());
            CompletableFuture<com.dto.OmdbMovieResponse> omdbFuture = movieApiService.getMovieDetails_omd(movieTitle);

            System.out.println("Scheduling TMDB API call at: " + System.currentTimeMillis());
            CompletableFuture<com.dto.TmdbMovieResponse> tmdbFuture = movieApiService.getMovieDetails_tmdb(movieTitle);

            // Wait for both API calls to complete
            CompletableFuture.allOf(omdbFuture, tmdbFuture).join();
            System.out.println("Both API calls completed at: " + System.currentTimeMillis());

            // Process OMDB response and insert into database
            try {
                com.dto.OmdbMovieResponse omdbResponse = omdbFuture.get();
                if (omdbResponse != null && omdbResponse.getError() == null) {
                    // Extract movie details from OMDB response
                    String title = omdbResponse.getTitle();
                    // Defensive parse for year
                    int year = 0;
                    try {
                        String yearStr = omdbResponse.getYear();
                        if (yearStr != null && !yearStr.equals("N/A")) {
                            year = Integer.parseInt(yearStr);
                        }
                    } catch (NumberFormatException e) {
                        year = 0;
                    }
                    String director = omdbResponse.getDirector();
                    String genre = omdbResponse.getGenre();
                    // Defensive parse for metascore
                    int metaScore = 0;
                    try {
                        String metaScoreStr = omdbResponse.getMetascore();
                        if (metaScoreStr != null && !metaScoreStr.equals("N/A")) {
                            metaScore = Integer.parseInt(metaScoreStr);
                        }
                    } catch (NumberFormatException e) {
                        metaScore = 0;
                    }

                    System.out.println("OMDB - Title: " + title);
                    System.out.println("OMDB - Year: " + year);
                    System.out.println("OMDB - Director: " + director);
                    System.out.println("OMDB - Genre: " + genre);

                    // Process TMDB response and download images in parallel
                    com.dto.TmdbMovieResponse tmdbResponse = tmdbFuture.get();
                    if (tmdbResponse != null && tmdbResponse.getResults() != null && !tmdbResponse.getResults().isEmpty()) {
                        com.dto.TmdbMovieResponse.TmdbResult item = tmdbResponse.getResults().get(0);
                        int movieId = item.getId();

                        // Get movie images from TMDB
                        String imageResponse = movieApiService.getMovieImages_tmdb(movieId).get();
                        JSONArray images = new JSONObject(imageResponse).getJSONArray("backdrops");

                        // Download up to 3 images in parallel
                        List<CompletableFuture<Void>> downloadFutures = new LinkedList<>();
                        List<String> imagesToDownload = new LinkedList<>();
                        for (int i = 0; i < Math.min(3, images.length()); i++) {
                            String imageUrl = "https://image.tmdb.org/t/p/w780/" + images.getJSONObject(i).getString("file_path");
                            imagesToDownload.add(imageUrl);
                            String filePath = "C:\\Development\\OOP3\\frameWork\\demo\\demo\\DownloadedImages\\"
                                    + item.getTitle().replace(" ", "_") + "_image" + (i + 1) + ".jpg";
                            System.out.println("Scheduling download for: " + imageUrl + " at " + System.currentTimeMillis());
                            downloadFutures.add(asyncImageDownloader.downloadImage(imageUrl, filePath));
                        }

                        // Wait for all image downloads to complete
                        CompletableFuture.allOf(downloadFutures.toArray(new CompletableFuture[0])).join();
                        System.out.println("All downloads completed at: " + System.currentTimeMillis());
    

                        // Insert movie into database with downloaded image paths
                        Movie movie = new com.model.Movie();
                        movie.setTitle(title);
                        movie.setYear(year);
                        movie.setDirector(director);
                        movie.setGenre(genre);
                        movie.setSimilarMovies("[]"); // Empty similar movies
                        movie.setImagePaths(new JSONArray(imagesToDownload).toString());
                        movie.setWatched(false);
                        movie.setRating((int)(1+(Math.floor((double)((double)metaScore/100)*4))));
                        // Use MovieService to save the movie entity
                        movieService.insertMovie(movie);

                        System.out.println("TMDB - ID: " + item.getId());
                        System.out.println("TMDB - Images: " + imagesToDownload);
                        System.out.println("TMDB - Title: " + item.getTitle());
                        System.out.println("TMDB - Release Date: " + item.getRelease_date());
                        System.out.println("TMDB - Overview: " + item.getOverview());
                    } else {
                        System.out.println("TMDB: No results found for " + movieTitle);
                    }
                } else {
                    System.out.println("OMDB: " + (omdbResponse != null ? omdbResponse.getError() : "Unknown error"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Movie '" + movieTitle + "' already exists in the database.");
        }

        // Retrieve and display all movies (for demonstration)
        dbManager.retrieveMovies(1,1);
        scanner.close();
    }

}

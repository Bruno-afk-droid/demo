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


    // CLI entry point: prompts user for a movie title, checks DB, and downloads if not present
    public void run() throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the movie title to download: ");
        String movieTitle = scanner.nextLine().trim();

        // Only download if movie is not already in the database
        if (!dbManager.isMovieInDatabase(movieTitle)) {
            handleMovieDownload(movieTitle);
        } else {
            System.out.println("Movie '" + movieTitle + "' already exists in the database.");
        }
        dbManager.retrieveMovies(1,1); // Example: retrieve movies for verification
        scanner.close();
    }

    /**
     * Downloads movie details and images from OMDB and TMDB, then inserts into DB.
     * Returns true if successful, false otherwise.
     */
    public boolean handleMovieDownload(String movieTitle) {
        System.out.println("Scheduling OMDB API call at: " + System.currentTimeMillis());
        CompletableFuture<com.model.OmdbMovieResponse> omdbFuture = movieApiService.getMovieDetails_omd(movieTitle);
        System.out.println("Scheduling TMDB API call at: " + System.currentTimeMillis());
        CompletableFuture<com.model.TmdbMovieResponse> tmdbFuture = movieApiService.getMovieDetails_tmdb(movieTitle);
        CompletableFuture.allOf(omdbFuture, tmdbFuture).join(); // Wait for both API calls
        System.out.println("Both API calls completed at: " + System.currentTimeMillis());
        try {
            com.model.OmdbMovieResponse omdbResponse = omdbFuture.get();
            if (omdbResponse != null && omdbResponse.getError() == null) {
                processOmdbAndTmdbResponses(omdbResponse, tmdbFuture, movieTitle);
                return true;
            } else {
                System.out.println("OMDB: " + (omdbResponse != null ? omdbResponse.getError() : "Unknown error"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    // Combines OMDB and TMDB responses, logs info, and triggers image download/DB insert
    private void processOmdbAndTmdbResponses(com.model.OmdbMovieResponse omdbResponse, CompletableFuture<com.model.TmdbMovieResponse> tmdbFuture, String movieTitle) throws Exception {
        String title = omdbResponse.getTitle();
        int year = parseIntSafe(omdbResponse.getYear());
        String director = omdbResponse.getDirector();
        String genre = omdbResponse.getGenre();
        int metaScore = parseIntSafe(omdbResponse.getMetascore());

        System.out.println("OMDB - Title: " + title);
        System.out.println("OMDB - Year: " + year);
        System.out.println("OMDB - Director: " + director);
        System.out.println("OMDB - Genre: " + genre);

        com.model.TmdbMovieResponse tmdbResponse = tmdbFuture.get();
        if (tmdbResponse != null && tmdbResponse.getResults() != null && !tmdbResponse.getResults().isEmpty()) {
            handleTmdbImagesAndInsert(tmdbResponse, title, year, director, genre, metaScore);
        } else {
            System.out.println("TMDB: No results found for " + movieTitle);
        }
    }

    // Downloads up to 3 images from TMDB, inserts movie into DB, and logs details
    private void handleTmdbImagesAndInsert(com.model.TmdbMovieResponse tmdbResponse, String title, int year, String director, String genre, int metaScore) throws Exception {
        com.model.TmdbMovieResponse.TmdbResult item = tmdbResponse.getResults().get(0);
        int movieId = item.getId();
        String imageResponse = movieApiService.getMovieImages_tmdb(movieId).get();
        JSONArray images = new JSONObject(imageResponse).getJSONArray("backdrops");
        List<CompletableFuture<Void>> downloadFutures = new LinkedList<>();
        List<String> imagesToDownload = new LinkedList<>();
        for (int i = 0; i < Math.min(3, images.length()); i++) {
            String imageUrl = "https://image.tmdb.org/t/p/w780/" + images.getJSONObject(i).getString("file_path");
            imagesToDownload.add(imageUrl);
            String safeTitle = item.getTitle() != null ? item.getTitle().replace(" ", "_") : "Unknown";
            String filePath = "C:\\Development\\OOP3\\frameWork\\demo\\demo\\DownloadedImages\\" + safeTitle + "_image" + (i + 1) + ".jpg";
            System.out.println("Scheduling download for: " + imageUrl + " at " + System.currentTimeMillis());
            downloadFutures.add(asyncImageDownloader.downloadImage(imageUrl, filePath));
        }
        CompletableFuture.allOf(downloadFutures.toArray(new CompletableFuture[0])).join(); // Wait for all downloads
        System.out.println("All downloads completed at: " + System.currentTimeMillis());

        // Build and insert the Movie object
        Movie movie = new com.model.Movie();
        movie.setTitle(title);
        movie.setYear(year);
        movie.setDirector(director);
        movie.setGenre(genre);
        movie.setSimilarMovies("[]");
        movie.setImagePaths(new JSONArray(imagesToDownload).toString());
        movie.setWatched(false);
        // Convert metascore (0-100) to rating (1-5)
        movie.setRating((int)(1+(Math.floor((double)((double)metaScore/100)*4))));
        movieService.insertMovieDB(movie);

        // Log TMDB details
        System.out.println("TMDB - ID: " + item.getId());
        System.out.println("TMDB - Images: " + imagesToDownload);
        System.out.println("TMDB - Title: " + item.getTitle());
        System.out.println("TMDB - Release Date: " + item.getRelease_date());
        System.out.println("TMDB - Overview: " + item.getOverview());
    }

    // Safely parse integer values, returns 0 if not possible
    private int parseIntSafe(String value) {
        try {
            if (value != null && !value.equals("N/A")) {
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            // ignore
        }
        return 0;
    }

}

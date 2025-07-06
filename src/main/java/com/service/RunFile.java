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

    
    public void run() throws Exception {
        // Prompt user for movie title
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the movie title to download: ");
        String movieTitle = scanner.nextLine().trim();

        // Check if movie exists in the database
        if (!dbManager.isMovieInDatabase(movieTitle)) {
            // Execute OMDB and TMDB API calls in parallel using CompletableFuture
            System.out.println("Scheduling OMDB API call at: " + System.currentTimeMillis());
            CompletableFuture<String> omdbFuture = movieApiService.getMovieDetails_omd(movieTitle);

            System.out.println("Scheduling TMDB API call at: " + System.currentTimeMillis());
            CompletableFuture<String> tmdbFuture = movieApiService.getMovieDetails_tmdb(movieTitle);

            // Wait for both API calls to complete
            CompletableFuture.allOf(omdbFuture, tmdbFuture).join();
            System.out.println("Both API calls completed at: " + System.currentTimeMillis());

            // Process OMDB response and insert into database
            try {
                String jsonResponseOMD = omdbFuture.get();
                if (jsonResponseOMD != null) {
                    JSONObject jsonObject = new JSONObject(jsonResponseOMD);
                    if (!jsonObject.has("Error")) {
                        // Extract movie details from OMDB response
                        String title = jsonObject.getString("Title");
                        int year = Integer.parseInt(jsonObject.getString("Year"));
                        String director = jsonObject.getString("Director");
                        String genre = jsonObject.getString("Genre");
                        int metaScore = jsonObject.getInt("Metascore");

                        System.out.println("OMDB - Title: " + title);
                        System.out.println("OMDB - Year: " + year);
                        System.out.println("OMDB - Director: " + director);
                        System.out.println("OMDB - Genre: " + genre);

                        // Process TMDB response and download images in parallel
                        String jsonResponseTMDB = tmdbFuture.get();
                        if (jsonResponseTMDB != null) {
                            JSONObject tmdbJson = new JSONObject(jsonResponseTMDB);
                            if (tmdbJson.getJSONArray("results").length() > 0) {
                                JSONObject item = tmdbJson.getJSONArray("results").getJSONObject(0);
                                int movieId = item.getInt("id");

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
                                            + item.getString("title").replace(" ", "_") + "_image" + (i + 1) + ".jpg";
                                    System.out.println("Scheduling download for: " + imageUrl + " at " + System.currentTimeMillis());
                                    downloadFutures.add(asyncImageDownloader.downloadImage(imageUrl, filePath));
                                }

                                // Wait for all image downloads to complete
                                CompletableFuture.allOf(downloadFutures.toArray(new CompletableFuture[0])).join();
                                System.out.println("All downloads completed at: " + System.currentTimeMillis());
    

                                // Insert movie into database with downloaded image paths
                                dbManager.insertMovie(
                                    title,
                                    year,
                                    director,
                                    genre,
                                    "[]", // Empty similar movies
                                    new JSONArray(imagesToDownload).toString(),
                                    false,
                                    (int)(1+(Math.floor((double)((double)metaScore/100)*4)))
                                );

                                System.out.println("TMDB - ID: " + item.getInt("id"));
                                System.out.println("TMDB - Images: " + imagesToDownload);
                                System.out.println("TMDB - Title: " + item.getString("title"));
                                System.out.println("TMDB - Release Date: " + item.getString("release_date"));
                                System.out.println("TMDB - Overview: " + item.getString("overview"));
                            } else {
                                System.out.println("TMDB: No results found for " + movieTitle);
                            }
                        }
                    } else {
                        System.out.println("OMDB: " + jsonObject.getString("Error"));
                    }
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

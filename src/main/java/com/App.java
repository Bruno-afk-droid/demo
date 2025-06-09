package com;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.model.MovieDBManager;

@SpringBootApplication
@EnableAsync

public class App {
    
    private static final String API_KEY_omd = "42138832"; // Replace with your OMDB API key
    private static final String BASE_URL_omd = "http://www.omdbapi.com/";
    private static final String BASE_URL_tmdb = "https://api.themoviedb.org/3/";
    private static final String API_KEY_tmdb = "2af894f45496ead36ca797704fb707b0";

    @Autowired
    private MovieApiService movieApiService;

    @Autowired
    private AsyncImageDownloader asyncImageDownloader;

    @Autowired
    @Lazy
    private MovieDBManager dbManager;

    public static void main(String[] args) throws BeansException, Exception {
        SpringApplication.run(App.class, args).getBean(App.class).run();
    }

    public void run() throws Exception {
        // Prompt user for movie title
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the movie title to download: ");
        String movieTitle = scanner.nextLine().trim();

        
        // Check if movie exists in database
        if (!dbManager.isMovieInDatabase(movieTitle)) {
            // Execute API calls in parallel
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

                                // Get movie images
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

                                // Insert movie into database
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

        // Retrieve and display all movies
        dbManager.retrieveMovies(1,1);
        scanner.close();
    }

 

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // Enough threads for API calls and downloads
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Async-Thread-");
        executor.initialize();
        return executor;
    }

    @Service
    public static class MovieApiService {
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

    @Service
    public static class AsyncImageDownloader {
        @Async("taskExecutor")
        public CompletableFuture<Void> downloadImage(String imageUrl, String filePath) {
            try (InputStream in = new URL(imageUrl).openStream();
                 OutputStream out = new FileOutputStream(filePath)) {
                System.out.println("Starting download for: " + imageUrl + " on thread: " + Thread.currentThread().getName() + " at " + System.currentTimeMillis());
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                System.out.println("Image saved at: " + filePath + " on thread: " + Thread.currentThread().getName() + " at " + System.currentTimeMillis());
                return CompletableFuture.completedFuture(null);
            } catch (IOException e) {
                e.printStackTrace();
                return CompletableFuture.completedFuture(null);
            }
        }
    }

    @Bean
    public MovieDBManager movieDBManager() {
        String url = "jdbc:mysql://localhost:3306/movies?serverTimezone=UTC";
        String user = "root";
        String password = "!Hofm100301";
        return new MovieDBManager(url, user, password);
    }
}

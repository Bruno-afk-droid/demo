package com.repository;

// Import necessary Java and Spring libraries for JDBC and collections
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

// Repository class for managing movie database operations
@Repository
public class MovieDBManager {
    // Database connection details
    private final String url;
    private final String user;
    private final String password;

    // Constructor to initialize connection details
    public MovieDBManager(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /**
     * Checks if a movie with the given title exists in the database.
     * @param title The movie title to check.
     * @return true if the movie exists, false otherwise.
     */
    public boolean isMovieInDatabase(String title) {
        String sql = "SELECT COUNT(*) FROM movies WHERE title = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Inserts a new movie into the database using a Movie object.
     * @param movie The Movie object to insert
     * @return true if insertion was successful, false otherwise
     */
    public boolean insertMovie(com.model.Movie movie) {
        String sql = "INSERT INTO movies (title, year, director, genre, similar_movies, image_paths, watched, rating) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, movie.getTitle());
            pstmt.setInt(2, movie.getYear());
            pstmt.setString(3, movie.getDirector());
            pstmt.setString(4, movie.getGenre());
            pstmt.setString(5, movie.getSimilarMovies());
            pstmt.setString(6, movie.getImagePaths());
            pstmt.setBoolean(7, movie.isWatched());
            pstmt.setInt(8, movie.getRating());
            pstmt.executeUpdate();
            System.out.println("Movie inserted successfully!");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves a paginated list of movie titles from the database.
     * @param page The page number (1-based)
     * @param pageSize The number of movies per page
     * @return List of movie titles for the requested page
     */
    public List<String> retrieveMovies(int page, int pageSize) {
        String sql = "SELECT * FROM movies";
        List<String> results = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            results = new ArrayList<>();
            while (rs.next()) {
                // Print all columns for debugging
                int columnCount = rs.getMetaData().getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rs.getMetaData().getColumnName(i);
                    String columnValue = rs.getString(i);
                    System.out.println(columnName + ": " + columnValue);
                }
                System.out.println("----------------------");
                // Add movie title to results
                results.add(rs.getString("title"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Pagination logic: skip and limit results
        if (page <= 0 || pageSize <= 0) {
            throw new IllegalArgumentException("Page and page size must be greater than 0");
        }
        return results.stream()
                    .skip((page - 1) * pageSize)
                    .limit(pageSize)
                    .collect(Collectors.toList());
    }

    /**
     * Updates the watched status and rating of a movie by title.
     * @param title Movie title
     * @param watched New watched status
     * @param rating New rating
     * @return true if update was successful, false otherwise
     */
    public boolean updateMovie(String title, boolean watched, int rating) {
        String sql = "UPDATE movies SET watched = ?, rating = ? WHERE title = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, watched);
            pstmt.setInt(2, rating);
            pstmt.setString(3, title);

            pstmt.executeUpdate();
            System.out.println("Movie updated successfully!");
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a movie from the database by title.
     * @param title Movie title
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteMovie(String title) {
        String sql = "DELETE FROM movies WHERE title = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title);
            pstmt.executeUpdate();
            System.out.println("Movie deleted successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    } 
}
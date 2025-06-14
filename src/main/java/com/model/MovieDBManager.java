package com.model;

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

@Repository
public class MovieDBManager {
    private final String url;
    private final String user;
    private final String password;

    public MovieDBManager(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    // Check if movie exists in database
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

    // CREATE: Insert new movie into the database
    public boolean insertMovie(String title, int year, String director, String genre, String similarMovies, String imagePaths, boolean watched, int rating) {
        String sql = "INSERT INTO movies (title, year, director, genre, similar_movies, image_paths, watched, rating) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title);
            pstmt.setInt(2, year);
            pstmt.setString(3, director);
            pstmt.setString(4, genre);
            pstmt.setString(5, similarMovies);  // Assuming JSON format
            pstmt.setString(6, imagePaths);  // Assuming JSON format
            pstmt.setBoolean(7, watched);
            pstmt.setInt(8, rating);
            
            pstmt.executeUpdate();
            System.out.println("Movie inserted successfully!");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // READ: Retrieve all movies from the database
    public List<String> retrieveMovies(int page, int pageSize) {
        String sql = "SELECT * FROM movies";
        List<String> results = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            results = new ArrayList<>();
            while (rs.next()) {
                int columnCount = rs.getMetaData().getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rs.getMetaData().getColumnName(i);
                    String columnValue = rs.getString(i);
                    System.out.println(columnName + ": " + columnValue);
                   

                }
                System.out.println("----------------------");
                
                results.add(rs.getString("title"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Pagination logic
        if (page <= 0 || pageSize <= 0) {
            throw new IllegalArgumentException("Page and page size must be greater than 0");
        }
        return results.stream()
                    .skip((page - 1) * pageSize)
                    .limit(pageSize)
                    .collect(Collectors.toList());
    }

    // UPDATE: Update movie rating & watched flag
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

    // DELETE: Remove a movie from the database
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
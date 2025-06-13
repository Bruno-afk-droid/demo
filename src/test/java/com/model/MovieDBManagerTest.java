package com.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase
public class MovieDBManagerTest {

    @Autowired
    private DataSource dataSource;

    private MovieDBManager movieDBManager;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize MovieDBManager with H2 connection details
        movieDBManager = new MovieDBManager(
            "jdbc:h2:mem:testdb",
            "sa",
            ""
        );

        // Create the movies table schema
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS movies (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "title VARCHAR(255), " +
                    "year INT, " +
                    "director VARCHAR(255), " +
                    "genre VARCHAR(255), " +
                    "similar_movies TEXT, " +
                    "image_paths TEXT, " +
                    "watched BOOLEAN, " +
                    "rating INT)"
            );
            // Clear the table
            stmt.executeUpdate("DELETE FROM movies");
        }
    }

    @Test
    void testIsMovieInDatabase_Exists() {
        // Insert a movie
        movieDBManager.insertMovie("Test Movie", 2020, "Test Director", "Action", "[]", "[\"image1.jpg\"]", false, 4);

        // Check if movie exists
        assertTrue(movieDBManager.isMovieInDatabase("Test Movie"));
    }

    @Test
    void testIsMovieInDatabase_NotExists() {
        // Check for a non-existent movie
        assertFalse(movieDBManager.isMovieInDatabase("Nonexistent Movie"));
    }

    @Test
    void testInsertMovie_Success() throws Exception {
        // Insert a movie
        movieDBManager.insertMovie("Test Movie", 2020, "Test Director", "Action", "[]", "[\"image1.jpg\"]", false, 4);

        // Verify insertion
        try (Connection conn = dataSource.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM movies WHERE title = ?")) {
            pstmt.setString(1, "Test Movie");
            java.sql.ResultSet rs = pstmt.executeQuery();
            assertTrue(rs.next());
            assertEquals("Test Movie", rs.getString("title"));
            assertEquals(2020, rs.getInt("year"));
            assertEquals("Test Director", rs.getString("director"));
            assertEquals("Action", rs.getString("genre"));
            assertEquals("[]", rs.getString("similar_movies"));
            assertEquals("[\"image1.jpg\"]", rs.getString("image_paths"));
            assertFalse(rs.getBoolean("watched"));
            assertEquals(4, rs.getInt("rating"));
        }
    }

    @Test
    void testRetrieveMovies_Pagination() {
        // Insert multiple movies
        movieDBManager.insertMovie("Movie 1", 2020, "Director 1", "Action", "[]", "[]", false, 4);
        movieDBManager.insertMovie("Movie 2", 2021, "Director 2", "Comedy", "[]", "[]", false, 3);
        movieDBManager.insertMovie("Movie 3", 2022, "Director 3", "Drama", "[]", "[]", true, 5);

        // Retrieve movies with pagination (page 1, size 2)
        List<String> moviesPage1 = movieDBManager.retrieveMovies(1, 2);
        assertEquals(2, moviesPage1.size());
        assertTrue(moviesPage1.contains("Movie 1"));
        assertTrue(moviesPage1.contains("Movie 2"));

        // Retrieve movies with pagination (page 2, size 2)
        List<String> moviesPage2 = movieDBManager.retrieveMovies(2, 2);
        assertEquals(1, moviesPage2.size());
        assertTrue(moviesPage2.contains("Movie 3"));
    }

    @Test
    void testUpdateMovie_Success() throws Exception {
        // Insert a movie
        movieDBManager.insertMovie("Test Movie", 2020, "Test Director", "Action", "[]", "[]", false, 4);

        // Get the movie ID
        int movieId;
        try (Connection conn = dataSource.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement("SELECT id FROM movies WHERE title = ?")) {
            pstmt.setString(1, "Test Movie");
            java.sql.ResultSet rs = pstmt.executeQuery();
            assertTrue(rs.next());
            movieId = rs.getInt("id");
        }

        // Update the movie
        movieDBManager.updateMovie("Test Movie", true, 5);

        // Verify update
        try (Connection conn = dataSource.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement("SELECT watched, rating FROM movies WHERE id = ?")) {
            pstmt.setInt(1, movieId);
            java.sql.ResultSet rs = pstmt.executeQuery();
            assertTrue(rs.next());
            assertTrue(rs.getBoolean("watched"));
            assertEquals(5, rs.getInt("rating"));
        }
    }

    @Test
    void testDeleteMovie_Success() throws Exception {
        // Insert a movie
        movieDBManager.insertMovie("Test Movie", 2020, "Test Director", "Action", "[]", "[]", false, 4);

        // Get the movie ID
        int movieId;
        try (Connection conn = dataSource.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement("SELECT id FROM movies WHERE title = ?")) {
            pstmt.setString(1, "Test Movie");
            java.sql.ResultSet rs = pstmt.executeQuery();
            assertTrue(rs.next());
            movieId = rs.getInt("id");
        }

        // Delete the movie
        movieDBManager.deleteMovie("Test Movie");

        // Verify deletion
        try (Connection conn = dataSource.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM movies WHERE id = ?")) {
            pstmt.setInt(1, movieId);
            java.sql.ResultSet rs = pstmt.executeQuery();
            rs.next();
            assertEquals(0, rs.getInt(1));
        }
    }
}
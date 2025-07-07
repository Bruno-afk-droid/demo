package com.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.repository.MovieDBManager;

@ExtendWith(MockitoExtension.class)
public class MovieDBManagerMockTest {

    @InjectMocks
    private MovieDBManager movieDBManager;

    @BeforeEach
    void setUp() {
        // Initialize MovieDBManager with mock connection details
        movieDBManager = new MovieDBManager("jdbc:mysql://localhost:3306/test", "root", "password");
    }

    @Test
    void testIsMovieInDatabase_Exists() throws Exception {
        // Mock DriverManager and JDBC components
        try (MockedStatic<DriverManager> driverManager = mockStatic(DriverManager.class)) {
            Connection connection = mock(Connection.class);
            PreparedStatement preparedStatement = mock(PreparedStatement.class);
            ResultSet resultSet = mock(ResultSet.class);

            driverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                .thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getInt(1)).thenReturn(1);

            // Test
            boolean exists = movieDBManager.isMovieInDatabase("Test Movie");
            assertTrue(exists);

            // Verify interactions
            verify(preparedStatement).setString(1, "Test Movie");
            verify(preparedStatement).executeQuery();
        }
    }

    @Test
    void testIsMovieInDatabase_NotExists() throws Exception {
        // Mock DriverManager and JDBC components
        try (MockedStatic<DriverManager> driverManager = mockStatic(DriverManager.class)) {
            Connection connection = mock(Connection.class);
            PreparedStatement preparedStatement = mock(PreparedStatement.class);
            ResultSet resultSet = mock(ResultSet.class);

            driverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                .thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getInt(1)).thenReturn(0);

            // Test
            boolean exists = movieDBManager.isMovieInDatabase("Test Movie");
            assertFalse(exists);

            // Verify interactions
            verify(preparedStatement).setString(1, "Test Movie");
        }
    }

    @Test
    void testInsertMovie_Success() throws Exception {
        // Mock DriverManager and JDBC components
        try (MockedStatic<DriverManager> driverManager = mockStatic(DriverManager.class)) {
            Connection connection = mock(Connection.class);
            PreparedStatement preparedStatement = mock(PreparedStatement.class);

            driverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                .thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(1);

            // Test
            com.model.Movie movie = new com.model.Movie();
            movie.setTitle("Test Movie");
            movie.setYear(2020);
            movie.setDirector("Test Director");
            movie.setGenre("Action");
            movie.setSimilarMovies("[]");
            movie.setImagePaths("[\"image1.jpg\"]");
            movie.setWatched(false);
            movie.setRating(4);
            movieDBManager.insertMovie(movie);

            // Verify interactions
            verify(preparedStatement).setString(1, "Test Movie");
            verify(preparedStatement).setInt(2, 2020);
            verify(preparedStatement).setString(3, "Test Director");
            verify(preparedStatement).setString(4, "Action");
            verify(preparedStatement).setString(5, "[]");
            verify(preparedStatement).setString(6, "[\"image1.jpg\"]");
            verify(preparedStatement).setBoolean(7, false);
            verify(preparedStatement).setInt(8, 4);
            verify(preparedStatement).executeUpdate();
        }
    }
}
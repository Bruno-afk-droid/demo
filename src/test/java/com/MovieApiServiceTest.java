package com;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
public class MovieApiServiceTest {

    @InjectMocks
    private App.MovieApiService movieApiService;

    @Mock
    private URL urlMock;

    @Mock
    private HttpURLConnection connectionMock;

    @Mock
    private BufferedReader readerMock;

    @BeforeEach
    void setUp() throws Exception {
        // Mock URL and connection behavior
        when(urlMock.openConnection()).thenReturn(connectionMock);
        when(connectionMock.getInputStream()).thenReturn(
                new java.io.ByteArrayInputStream("{\"Title\":\"Test Movie\",\"Year\":\"2020\"}".getBytes()));
        when(readerMock.readLine()).thenReturn("{\"Title\":\"Test Movie\",\"Year\":\"2020\"}", null);
    }

    @Test
    void testGetMovieDetailsOmdb_Success() throws Exception {
        // Mock URL creation
        try (var urlConstructor = mockConstruction(URL.class, (mock, context) -> {
            when(mock.openConnection()).thenReturn(connectionMock);
        })) {
            CompletableFuture<String> result = movieApiService.getMovieDetails_omd("Test Movie");

            // Verify result
            assertNotNull(result.get());
            assertTrue(result.get().contains("Test Movie"));
        }

        // Verify interactions
        verify(connectionMock).setRequestMethod("GET");
    }

    @Test
    void testGetMovieDetailsOmdb_Failure() throws Exception {
        // Simulate exception
        when(urlMock.openConnection()).thenThrow(new java.io.IOException("Connection failed"));

        try (var urlConstructor = mockConstruction(URL.class, (mock, context) -> {
            when(mock.openConnection()).thenThrow(new java.io.IOException("Connection failed"));
        })) {
            CompletableFuture<String> result = movieApiService.getMovieDetails_omd("Test Movie");

            // Verify result
            assertNull(result.get());
        }
    }
}
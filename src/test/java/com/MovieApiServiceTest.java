package com;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MovieApiServiceTest {

    @InjectMocks
    private com.service.MovieApiService movieApiService = new com.service.MovieApiService();

    @Test
    void testGetMovieDetailsOmdb_Success() throws Exception {
        String json = "{\"Title\":\"Test Movie\",\"Year\":\"2020\"}";
        try (var urlConstructor = mockConstruction(URL.class, (mock, context) -> {
            HttpURLConnection connectionMock = mock(HttpURLConnection.class);
            when(mock.openConnection()).thenReturn(connectionMock);
            lenient().when(connectionMock.getInputStream()).thenReturn(new ByteArrayInputStream(json.getBytes()));
            lenient().when(connectionMock.getResponseCode()).thenReturn(200);
            lenient().when(connectionMock.getRequestMethod()).thenReturn("GET");
        })) {
            CompletableFuture<String> result = movieApiService.getMovieDetails_omd("Test Movie");
            assertNotNull(result.get());
            assertTrue(result.get().contains("Test Movie"));
        }
    }

    @Test
    void testGetMovieDetailsOmdb_Failure() throws Exception {
        try (var urlConstructor = mockConstruction(URL.class, (mock, context) -> {
            when(mock.openConnection()).thenThrow(new java.io.IOException("Connection failed"));
        })) {
            CompletableFuture<String> result = movieApiService.getMovieDetails_omd("Test Movie");
            assertNull(result.get());
        }
    }
}
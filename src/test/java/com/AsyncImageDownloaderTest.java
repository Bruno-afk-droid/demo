package com;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AsyncImageDownloaderTest {

    @InjectMocks
    private App.AsyncImageDownloader asyncImageDownloader;

    @Mock
    private URL urlMock;

    @Mock
    private InputStream inputStreamMock;

    @Mock
    private OutputStream outputStreamMock;

    @Test
    void testDownloadImage_Success() throws Exception {
        // Mock URL and streams
        try (var urlConstructor = mockConstruction(URL.class, (mock, context) -> {
            when(mock.openStream()).thenReturn(inputStreamMock);
        })) {
            when(inputStreamMock.read(any(byte[].class))).thenReturn(1024, -1); // Simulate reading data

            CompletableFuture<Void> result = asyncImageDownloader.downloadImage("http://test.com/image.jpg", "test.jpg");

            // Verify result
            assertNull(result.get()); // Void future returns null on success

            // Verify interactions
            verify(inputStreamMock, atLeastOnce()).read(any(byte[].class));
            verify(outputStreamMock, atLeastOnce()).write(any(byte[].class), eq(0), eq(1024));
        }
    }

    @Test
    void testDownloadImage_Failure() throws Exception {
        // Simulate exception
        try (var urlConstructor = mockConstruction(URL.class, (mock, context) -> {
            when(mock.openStream()).thenThrow(new java.io.IOException("Download failed"));
        })) {
            CompletableFuture<Void> result = asyncImageDownloader.downloadImage("http://test.com/image.jpg", "test.jpg");

            // Verify result
            assertNull(result.get()); // Exception handled, returns null
        }
    }
}
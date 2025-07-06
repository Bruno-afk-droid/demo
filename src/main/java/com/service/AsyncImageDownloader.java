package com.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncImageDownloader {
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

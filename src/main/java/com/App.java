package com;
// Import necessary Java and Spring libraries for HTTP, concurrency, JSON, and Spring Boot
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.repository.MovieDBManager;
import com.service.RunFile;

// Main Spring Boot application class
@SpringBootApplication
@EnableAsync // Enable asynchronous method execution
public class App {
    // API keys and base URLs for OMDB and TMDB
    private static final String API_KEY_omd = "42138832"; // Replace with your OMDB API key
    private static final String BASE_URL_omd = "http://www.omdbapi.com/";
    private static final String BASE_URL_tmdb = "https://api.themoviedb.org/3/";
    private static final String API_KEY_tmdb = "2af894f45496ead36ca797704fb707b0";

    @Autowired
    private RunFile runFile;

    // Main entry point for the Spring Boot application
    public static void main(String[] args) throws BeansException, Exception {
        SpringApplication.run(App.class, args).getBean(RunFile.class)
        .run();
    }
}

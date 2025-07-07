package com;
// Import necessary Java and Spring libraries for HTTP, concurrency, JSON, and Spring Boot
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import com.service.RunFile;

// Main Spring Boot application class
@SpringBootApplication
@EnableAsync // Enable asynchronous method execution
public class App {

    // Main entry point for the Spring Boot application
    public static void main(String[] args) throws BeansException, Exception {
        SpringApplication.run(App.class, args).getBean(RunFile.class)
        .run();
    }
}

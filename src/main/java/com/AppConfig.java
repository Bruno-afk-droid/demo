package com;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import com.repository.MovieDBManager;

@Configuration
public class AppConfig {
    // Configure a thread pool for async operations (API calls, downloads)
    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // Enough threads for API calls and downloads
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Async-Thread-");
        executor.initialize();
        return executor;
    }

    // Bean for creating the MovieDBManager with MySQL connection details
    @Bean
    public MovieDBManager movieDBManager() {
        String url = "jdbc:mysql://localhost:3306/movies?serverTimezone=UTC";
        String user = "root";
        String password = "!Hofm100301";
        return new MovieDBManager(url, user, password);
    }

    // Bean for RestTemplate to be used with MovieApiService
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

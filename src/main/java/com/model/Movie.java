package com.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;
    private int year;
    private String director;
    private String genre;
    @Column(name = "similar_movies", columnDefinition = "TEXT")
    private String similarMovies;
    @Column(name = "image_paths", columnDefinition = "TEXT")
    private String imagePaths;
    private boolean watched;
    private int rating;
}

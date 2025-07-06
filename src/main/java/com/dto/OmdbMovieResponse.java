package com.dto;

public class OmdbMovieResponse {
    private String Title;
    private String Year;
    private String Director;
    private String Genre;
    private String Metascore;
    private String Error;
    // ...add other fields as needed

    // Getters and setters
    public String getTitle() { return Title; }
    public void setTitle(String title) { Title = title; }
    public String getYear() { return Year; }
    public void setYear(String year) { Year = year; }
    public String getDirector() { return Director; }
    public void setDirector(String director) { Director = director; }
    public String getGenre() { return Genre; }
    public void setGenre(String genre) { Genre = genre; }
    public String getMetascore() { return Metascore; }
    public void setMetascore(String metascore) { Metascore = metascore; }
    public String getError() { return Error; }
    public void setError(String error) { Error = error; }
}

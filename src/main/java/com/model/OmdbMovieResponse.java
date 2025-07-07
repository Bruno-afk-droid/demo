
package com.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class OmdbMovieResponse {
    @JsonProperty("Title")
    private String title;
    @JsonProperty("Year")   
    private String year;
    @JsonProperty("Director")
    private String director;
    @JsonProperty("Genre")
    private String genre;
    @JsonProperty("Metascore")
    private String metascore;
    @JsonProperty("Error")
    private String error;
    // ...add other fields as needed, with @JsonProperty if names differ
}

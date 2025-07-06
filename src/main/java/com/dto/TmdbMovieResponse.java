package com.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class TmdbMovieResponse {
    private List<TmdbResult> results;

    public List<TmdbResult> getResults() { return results; }
    public void setResults(List<TmdbResult> results) { this.results = results; }

    public static class TmdbResult {
        private int id;
        @JsonProperty("Title")
        private String title;
        @JsonProperty("Released")
        private String releaseDate;
        private String overview;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getRelease_date() { return releaseDate; }
        public void setRelease_date(String releaseDate) { this.releaseDate = releaseDate; }
        public String getOverview() { return overview; }
        public void setOverview(String overview) { this.overview = overview; }
    }
}

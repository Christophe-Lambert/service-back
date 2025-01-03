package com.mapviewer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;



@JsonIgnoreProperties(ignoreUnknown = true)
public class GeocodingResponse {
    @JsonProperty("results")
    private GeocodingResult[] results;

    @JsonProperty("status")
    private String status;

    public GeocodingResult[] getResults() {
        return results;
    }

    public void setResults(GeocodingResult[] results) {
        this.results = results;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

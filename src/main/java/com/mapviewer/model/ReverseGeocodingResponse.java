package com.mapviewer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReverseGeocodingResponse {
    private String type;
    private String version;
    private List<Feature> features;

    @Data
    public static class Feature {
        private String type;
        private Geometry geometry;
        private Properties properties;
    }

    @Data
    public static class Geometry {
        private String type;
        private List<Double> coordinates;
    }

    @Data
    public static class Properties {
        private String label;
        private String city;
        private String postcode;
        private String street;
        private String housenumber;
    }
}

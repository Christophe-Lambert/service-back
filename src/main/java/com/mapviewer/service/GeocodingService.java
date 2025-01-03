package com.mapviewer.service;

import com.mapviewer.model.GeocodingResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Service
public class GeocodingService {

    private final WebClient webClient;

    public GeocodingService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://maps.googleapis.com/maps/api/geocode").build();
    }

    public Mono<String> reverseGeocode(double lat, double lng, String apiKey) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/json")
                        .queryParam("latlng", lat + "," + lng)
                        .queryParam("key", apiKey)
                        .queryParam("result_type", "street_address")
                        .build())
                .retrieve()
                .bodyToMono(GeocodingResponse.class)
                .map(response -> {
                    if ("OK".equals(response.getStatus())) {
                        return Arrays.stream(response.getResults())
                                .map(geocodingResult -> geocodingResult.getFormattedAddress())
                                .findFirst()
                                .orElse("No address found");
                    }
                    return "Error: " + response.getStatus();
                });
    }

}

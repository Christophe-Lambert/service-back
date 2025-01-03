package com.mapviewer.controller;

import com.mapviewer.model.api.Coordinate;
import com.mapviewer.service.GeocodingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/geocoding")
@RequiredArgsConstructor
public class GeocodingController {
    private final GeocodingService geocodingService;

    @Value("${google.api.key}")
    private String apiKey;

    @PostMapping("/reverse-bulk")
    public Flux<String> reverseGeocodeBulk(@RequestBody List<Coordinate> coordinates) {
        return Flux.fromIterable(coordinates)
                .flatMap(coord -> geocodingService.reverseGeocode(coord.getLat(), coord.getLng(), apiKey));
    }
}

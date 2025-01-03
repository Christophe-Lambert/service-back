package com.mapviewer.controller;

import com.mapviewer.model.ReverseGeocodingResponse;
import com.mapviewer.service.BulkReverseGeocodingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/geocoding")
@RequiredArgsConstructor
public class BulkGeocodingController {
    private final BulkReverseGeocodingService geocodingService;

    @PostMapping("/bulk-reverse")
    public ResponseEntity<List<ReverseGeocodingResponse>> reverseGeocodeBulk(@RequestBody List<double[]> coordinates) {
        List<ReverseGeocodingResponse> results = geocodingService.getAddressesInBulk(coordinates);

        return ResponseEntity.ok(results);
    }

    @PostMapping("/reverse")
    public ResponseEntity<ReverseGeocodingResponse> reverseGeocode(@RequestParam double latitude, @RequestParam double longitude) {
        var result = geocodingService.getReverseGeocodingResponse(latitude, longitude);
        return ResponseEntity.ok(result);
    }
}

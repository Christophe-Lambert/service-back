package com.mapviewer.controller;

import com.mapviewer.model.api.Coordinate;
import com.mapviewer.service.FlaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/analysis")
@RequiredArgsConstructor
public class AnalysisController {
    @Value("${analysis.url}")
    private String analysisUrl;

    private final FlaskService flaskService;

    @PostMapping("/positions")
    public ResponseEntity<?> analyzePositions(@RequestBody String query) {
        RestTemplate restTemplate = new RestTemplate();
        //String url = "http://localhost:5000/analyze"; // URL du service Python Flask

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);  // Changer le type à TEXT_PLAIN

        // Nous envoyons directement la requête AQL brute dans le corps de la requête
        HttpEntity<String> entity = new HttpEntity<>(query, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(analysisUrl + "/analyze", entity, String.class);

        // Retourner la réponse du service Python
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @PostMapping("/detect-anomalies")
    public Map<String, Object> detectAnomalies(@RequestBody List<Coordinate> coordinates) {
        // Log des coordonnées reçues
        coordinates.forEach(coord -> System.out.println("Lat: " + coord.getLat() + ", Lng: " + coord.getLng()));

        // Convertir en tableau double[][] si nécessaire
        double[][] positions = coordinates.stream()
                .map(coord -> new double[]{coord.getLat(), coord.getLng()})
                .toArray(double[][]::new);

        // Appeler le service Flask
        return flaskService.detectAnomalies(positions);
    }

    @PostMapping("/detect-temporal-anomalies")
    public Object detectTemporalAnomalies(@RequestBody Object data) {
        // Appeler le service Flask pour détecter les anomalies temporelles
        return flaskService.detectTemporalAnomalies(data);
    }
}

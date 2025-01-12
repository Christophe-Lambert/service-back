package com.mapviewer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class FlaskService {
    @Value("${analysis.url}")
    private String analysisUrl;

    @Autowired
    private RestTemplate restTemplate;

    //private final String FLASK_BASE_URL = "http://localhost:5000"; // URL de l'API Flask

    public Map<String, Object> detectAnomalies(double[][] positions) {
        // Construire la requête JSON
        Map<String, Object> request = new HashMap<>();
        request.put("positions", positions);

        // Effectuer l'appel POST
        String url = analysisUrl + "/detect-anomalies"; // Endpoint Flask
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        // Retourner la réponse
        return response.getBody();
    }

    public Object detectTemporalAnomalies(Object data) {
        // L'URL du service Flask
        String url = "http://localhost:5000/detect-temporal-anomalies";

        // Faire l'appel HTTP POST à l'API Flask
        HttpEntity<Object> request = new HttpEntity<>(data);
        ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.POST, request, Object.class);

        // Retourner la réponse (anomalies détectées ou erreur)
        return response.getBody();
    }
}

package com.mapviewer.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapviewer.model.ReverseGeocodingResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class BulkReverseGeocodingService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String API_URL = "https://api-adresse.data.gouv.fr/reverse/";

    public String getAddressFromCoordinates(double latitude, double longitude) {
        try {
            String url = String.format(Locale.US,"%s?lon=%f&lat=%f", API_URL, longitude, latitude);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to fetch address: " + response.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ReverseGeocodingResponse getReverseGeocodingResponse(double latitude, double longitude) {
        try {
            // Construire l'URL avec les coordonnées
            String apiUrlWithParams = String.format(Locale.US,"%s?lon=%f&lat=%f", API_URL, longitude, latitude);

            // Appeler l'API
            URL url = new URL(apiUrlWithParams);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            // Lire la réponse
            if (connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Désérialiser la réponse JSON en objet ReverseGeocodingResponse
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                var result = objectMapper.readValue(response.toString(), ReverseGeocodingResponse.class);

                return result;
            } else {
                throw new RuntimeException("HTTP error code : " + connection.getResponseCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'appel à l'API : " + e.getMessage());
        }
    }

    public List<ReverseGeocodingResponse> getAddressesInBulk(List<double[]> coordinates) {
        List<ReverseGeocodingResponse> results = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(10); // Limiter à 10 threads

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (double[] coord : coordinates) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                //String address = getAddressFromCoordinates(coord[0], coord[1]);
                var address = getReverseGeocodingResponse(coord[0], coord[1]);
                synchronized (results) {
                    results.add(address);
                }
            }, executor);
            futures.add(future);
        }

        // Attendre que toutes les tâches soient terminées
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        executor.shutdown();
        return results;
    }
}
